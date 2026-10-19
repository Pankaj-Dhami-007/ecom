package com.dhami.orderservice.order.service;

import com.dhami.orderservice.client.catalog.ProductClient;
import com.dhami.orderservice.common.context.RequestUserContext;
import com.dhami.orderservice.kafka.OrderEventPublisher;
import com.dhami.orderservice.order.dto.CreateOrderRequest;
import com.dhami.orderservice.order.dto.OrderItemRequest;
import com.dhami.orderservice.order.dto.ShippingAddressRequest;
import com.dhami.orderservice.client.catalog.dto.StockUpdateRequest;
import com.dhami.orderservice.order.dto.UpdateOrderRequest;
import com.dhami.orderservice.order.dto.UpdateOrderStatusRequest;
import com.dhami.orderservice.common.dto.ApiResponse;
import com.dhami.orderservice.order.dto.OrderResponse;
import com.dhami.orderservice.common.dto.PageResponse;
import com.dhami.orderservice.client.catalog.dto.ProductResponse;
import com.dhami.orderservice.order.OrderEntity;
import com.dhami.orderservice.order.OrderItemEntity;
import com.dhami.orderservice.order.OrderStatus;
import com.dhami.orderservice.order.OrderServiceException;
import com.dhami.orderservice.order.ShippingAddressEntity;
import com.dhami.orderservice.order.event.OrderPaymentRequestedEvent;
import com.dhami.orderservice.common.exception.ResourceNotFoundException;
import com.dhami.orderservice.order.mapper.OrderMapper;
import com.dhami.orderservice.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductClient productClient;

    // OLD: sent notifications over HTTP through Feign, inside the order transaction.
    // private final OrderNotificationService orderNotificationService;

    private final OrderEventPublisher orderEventPublisher;

    private final ApplicationEventPublisher publisher;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING_PAYMENT, Set.of(OrderStatus.CONFIRMED, OrderStatus.PAYMENT_FAILED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.PAYMENT_FAILED, Set.of(OrderStatus.PENDING_PAYMENT, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, Set.of());
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, Set.of());
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Long userId = RequestUserContext.requireUserId();
        log.info("Creating order for customer id: {} with {} item(s)", userId, request.getItems().size());

        List<OrderItemEntity> items = buildOrderItems(request.getItems());

        BigDecimal total = items.stream()
                .map(OrderItemEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderEntity order = OrderEntity.builder()
                .orderNumber(generateOrderNumber())
                .customerId(userId)
                .cartId(request.getCartId())
                .totalAmount(total)
                .status(OrderStatus.PENDING_PAYMENT)
                .shippingAddress(buildShippingAddress(request.getShippingAddress()))
                .build();

        items.forEach(order::addItem);

        OrderEntity saved = orderRepository.save(order);
        reserveStockForItems(saved.getOrderItems());

        // OLD: blocked this transaction on an SMTP round trip to notification-service.
        // orderNotificationService.onOrderCreated(saved);

        orderEventPublisher.publishCreated(saved);

        requestPayment(saved);
        log.info("Order created successfully with id: {} and number: {}", saved.getId(), saved.getOrderNumber());
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse retryPayment(Long id) {
        log.info("Retrying payment for order id: {}", id);
        OrderEntity order = findOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            log.info("Order {} is PAYMENT_FAILED - taking a fresh stock reservation before retry", id);
            reserveStockForItems(order.getOrderItems());
            order.setStatus(OrderStatus.PENDING_PAYMENT);
        } else if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            log.info("Order {} is still PENDING_PAYMENT - retrying without reserving again, the hold is still in place", id);
        } else {
            log.warn("Payment retry failed - order {} status is {}", id, order.getStatus());
            throw new OrderServiceException(
                    "Payment can only be retried when status is PAYMENT_FAILED or PENDING_PAYMENT. Current status: "
                            + order.getStatus()
            );
        }

        OrderEntity saved = orderRepository.save(order);

        requestPayment(saved);
        log.info("Payment retry completed for order id: {} with status {}", id, saved.getStatus());
        return orderMapper.toResponse(saved);
    }

    private void requestPayment(OrderEntity order) {
        publisher.publishEvent(new OrderPaymentRequestedEvent(order.getId(), order.getOrderNumber()));
    }

    @Override
    @Transactional
    public OrderResponse confirmOrder(Long id) {
        log.info("Confirming order id: {} after successful payment", id);
        OrderEntity order = findOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.warn("Order {} is already CONFIRMED - confirm callback ignored", id);
            return orderMapper.toResponse(order);
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("Order confirm failed - order {} status is {}", id, order.getStatus());
            throw new OrderServiceException(
                    "Order can only be confirmed when status is PENDING_PAYMENT. Current status: " + order.getStatus()
            );
        }

        confirmStockForItems(order.getOrderItems());
        order.setStatus(OrderStatus.CONFIRMED);
        OrderEntity saved = orderRepository.save(order);
        log.info("Order {} confirmed - reserved stock is now sold", id);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse markPaymentFailed(Long id) {
        log.info("Marking payment as failed for order id: {}", id);
        OrderEntity order = findOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            log.warn("Order {} is already PAYMENT_FAILED - failure callback ignored", id);
            return orderMapper.toResponse(order);
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("Payment failure callback rejected - order {} status is {}", id, order.getStatus());
            throw new OrderServiceException(
                    "Payment failure can only be applied when status is PENDING_PAYMENT. Current status: " + order.getStatus()
            );
        }

        releaseStockForItems(order.getOrderItems());
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        OrderEntity saved = orderRepository.save(order);
        log.info("Order {} moved to PAYMENT_FAILED - reserved stock released", id);
        return orderMapper.toResponse(saved);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order by id: {}", id);
        OrderEntity order = findOrderOrThrow(id);
        return orderMapper.toResponse(order);
    }

    @Override
    public PageResponse<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        log.info("Fetching orders for customer id: {} - page: {}, size: {}", customerId, pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(
                orderRepository.findByCustomerId(customerId, pageable)
                        .map(orderMapper::toResponse)
        );
    }

    @Override
    public PageResponse<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.info("Fetching orders by status: {} - page: {}, size: {}", status, pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(
                orderRepository.findByStatus(status, pageable)
                        .map(orderMapper::toResponse)
        );
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, UpdateOrderRequest request) {
        log.info("Updating order with id: {}", id);
        OrderEntity order = findOrderOrThrow(id);

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("Order update failed - order {} status is {}", id, order.getStatus());
            throw new OrderServiceException(
                    "Order can only be updated when status is PENDING_PAYMENT. Current status: " + order.getStatus()
            );
        }

        List<OrderItemEntity> newItems = buildOrderItems(request.getItems());

        // the current items are still sitting in reserved, so hand them back
        // before holding a reservation for the new set
        releaseStockForItems(order.getOrderItems());
        reserveStockForItems(newItems);

        order.getOrderItems().clear();
        newItems.forEach(order::addItem);

        BigDecimal total = newItems.stream()
                .map(OrderItemEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        OrderEntity saved = orderRepository.save(order);
        log.info("Order updated successfully with id: {}", id);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        log.info("Cancelling order with id: {}", id);
        OrderEntity order = findOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Order already cancelled - order id: {}", id);
            throw new OrderServiceException("Order is already cancelled");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            log.warn("Order cancellation failed - order {} status is {}", id, order.getStatus());
            throw new OrderServiceException(
                    "Order cannot be cancelled when status is DELIVERED. Current status: " + order.getStatus()
            );
        }

        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            // stock is held in reserved but was never sold, so it goes back to available
            releaseStockForItems(order.getOrderItems());
        } else if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            // the reservation was already given back when the payment failed
            log.info("Order {} cancelled from PAYMENT_FAILED - stock was already released", id);
        } else {
            // stock was confirmed as sold, so it has to be returned
            restoreStockForItems(order.getOrderItems());
        }
        order.setStatus(OrderStatus.CANCELLED);
        OrderEntity saved = orderRepository.save(order);

        // OLD: blocked this transaction on an SMTP round trip to notification-service.
        // orderNotificationService.onOrderCancelled(saved);

        orderEventPublisher.publishCancelled(saved);

        log.info("Order cancelled successfully with id: {}", id);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        log.info("Updating status for order id: {} to {}", id, request.getStatus());
        OrderEntity order = findOrderOrThrow(id);

        OrderStatus current = order.getStatus();
        OrderStatus target = request.getStatus();

        if (current == target) {
            log.warn("Order status update skipped - order {} already has status {}", id, current);
            throw new OrderServiceException("Order already has status: " + current);
        }

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.get(current);
        if (allowed == null || allowed.isEmpty()) {
            log.warn("Order status update failed - order {} is in terminal state {}", id, current);
            throw new OrderServiceException(
                    "Cannot transition from terminal state: " + current
            );
        }

        if (!allowed.contains(target)) {
            log.warn("Order status update failed - cannot transition from {} to {} for order {}", current, target, id);
            throw new OrderServiceException(
                    "Cannot transition from " + current + " to " + target
            );
        }

        order.setStatus(target);
        OrderEntity saved = orderRepository.save(order);

        if (target == OrderStatus.DELIVERED) {

            // OLD: blocked this transaction on an SMTP round trip to notification-service.
            // orderNotificationService.onOrderDelivered(saved);

            orderEventPublisher.publishDelivered(saved);
        }

        log.info("Order status updated successfully for id: {} from {} to {}", id, current, target);
        return orderMapper.toResponse(saved);
    }

    private ShippingAddressEntity buildShippingAddress(ShippingAddressRequest request) {
        return ShippingAddressEntity.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .build();
    }

    private OrderEntity findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private List<OrderItemEntity> buildOrderItems(List<OrderItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(this::buildOrderItem)
                .toList();
    }

    private OrderItemEntity buildOrderItem(OrderItemRequest itemRequest) {
        log.debug("Validating product id: {} with quantity: {}", itemRequest.getProductId(), itemRequest.getQuantity());
        ApiResponse<ProductResponse> response  = productClient.getProductById(itemRequest.getProductId());
        ProductResponse product = response.getData();
        if (!product.getActive()) {
            log.warn("Product validation failed - product not active: {}", product.getName());
            throw new OrderServiceException("Product is not active: " + product.getName());
        }

        if (product.getStockQuantity() < itemRequest.getQuantity()) {
            log.warn("Product validation failed - insufficient stock for: {}. Available: {}, Requested: {}",
                    product.getName(), product.getStockQuantity(), itemRequest.getQuantity());
            throw new OrderServiceException(
                    "Insufficient stock for product: " + product.getName()
                            + ". Available: " + product.getStockQuantity()
                            + ", Requested: " + itemRequest.getQuantity()
            );
        }

        BigDecimal price = product.getPrice();
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

        return OrderItemEntity.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(itemRequest.getQuantity())
                .price(price)
                .subtotal(subtotal)
                .build();
    }

    private void reserveStockForItems(List<OrderItemEntity> items) {
        log.debug("Reserving stock for {} item(s)", items.size());
        for (OrderItemEntity item : items) {
            StockUpdateRequest request = new StockUpdateRequest(item.getQuantity());
            productClient.reserveStock(item.getProductId(), request);
            log.debug("Stock reserved for product id: {}, quantity: {}", item.getProductId(), item.getQuantity());
        }
    }

    private void confirmStockForItems(List<OrderItemEntity> items) {
        log.debug("Confirming reserved stock for {} item(s)", items.size());
        for (OrderItemEntity item : items) {
            StockUpdateRequest request = new StockUpdateRequest(item.getQuantity());
            productClient.confirmReservation(item.getProductId(), request);
            log.debug("Reservation confirmed for product id: {}, quantity: {}", item.getProductId(), item.getQuantity());
        }
    }

    private void releaseStockForItems(List<OrderItemEntity> items) {
        log.debug("Releasing reserved stock for {} item(s)", items.size());
        for (OrderItemEntity item : items) {
            StockUpdateRequest request = new StockUpdateRequest(item.getQuantity());
            productClient.releaseReservation(item.getProductId(), request);
            log.debug("Stock reservation released for product id: {}, quantity: {}", item.getProductId(), item.getQuantity());
        }
    }

    private void restoreStockForItems(List<OrderItemEntity> items) {
        log.debug("Restoring stock for {} item(s)", items.size());
        for (OrderItemEntity item : items) {
            StockUpdateRequest request = new StockUpdateRequest(item.getQuantity());
            productClient.restoreStock(item.getProductId(), request);
            log.debug("Stock restored for product id: {}, quantity: {}", item.getProductId(), item.getQuantity());
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
