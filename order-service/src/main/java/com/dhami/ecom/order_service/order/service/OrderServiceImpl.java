package com.dhami.ecom.order_service.order.service;

import com.dhami.ecom.order_service.client.product.ProductClient;
import com.dhami.ecom.order_service.common.context.RequestUserContext;
import com.dhami.ecom.order_service.order.dto.CreateOrderRequest;
import com.dhami.ecom.order_service.order.dto.OrderItemRequest;
import com.dhami.ecom.order_service.client.product.dto.StockUpdateRequest;
import com.dhami.ecom.order_service.order.dto.UpdateOrderRequest;
import com.dhami.ecom.order_service.order.dto.UpdateOrderStatusRequest;
import com.dhami.ecom.order_service.common.dto.ApiResponse;
import com.dhami.ecom.order_service.order.dto.OrderResponse;
import com.dhami.ecom.order_service.common.dto.PageResponse;
import com.dhami.ecom.order_service.client.product.dto.ProductResponse;
import com.dhami.ecom.order_service.order.OrderEntity;
import com.dhami.ecom.order_service.order.OrderItemEntity;
import com.dhami.ecom.order_service.order.OrderStatus;
import com.dhami.ecom.order_service.order.OrderServiceException;
import com.dhami.ecom.order_service.common.exception.ResourceNotFoundException;
import com.dhami.ecom.order_service.order.mapper.OrderMapper;
import com.dhami.ecom.order_service.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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
    private final OrderNotificationService orderNotificationService;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
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
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .build();

        items.forEach(order::addItem);

        OrderEntity saved = orderRepository.save(order);
        deductStockForItems(saved.getOrderItems());
        orderNotificationService.onOrderCreated(saved);
        log.info("Order created successfully with id: {} and number: {}", saved.getId(), saved.getOrderNumber());
        return orderMapper.toResponse(saved);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order by id: {}", id);
        OrderEntity order = findOrderOrThrow(id);
        return orderMapper.toResponse(order);
    }

    @Override
    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(
                orderRepository.findAll(pageable).map(orderMapper::toResponse)
        );
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

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order update failed - order {} status is {}", id, order.getStatus());
            throw new OrderServiceException(
                    "Order can only be updated when status is PENDING. Current status: " + order.getStatus()
            );
        }

        List<OrderItemEntity> newItems = buildOrderItems(request.getItems());
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

        restoreStockForItems(order.getOrderItems());
        order.setStatus(OrderStatus.CANCELLED);
        OrderEntity saved = orderRepository.save(order);
        orderNotificationService.onOrderCancelled(saved);
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
            orderNotificationService.onOrderDelivered(saved);
        }

        log.info("Order status updated successfully for id: {} from {} to {}", id, current, target);
        return orderMapper.toResponse(saved);
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

    private void deductStockForItems(List<OrderItemEntity> items) {
        log.debug("Deducting stock for {} item(s)", items.size());
        for (OrderItemEntity item : items) {
            StockUpdateRequest request = new StockUpdateRequest(item.getQuantity());
            productClient.deductStock(item.getProductId(), request);
            log.debug("Stock deducted for product id: {}, quantity: {}", item.getProductId(), item.getQuantity());
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
