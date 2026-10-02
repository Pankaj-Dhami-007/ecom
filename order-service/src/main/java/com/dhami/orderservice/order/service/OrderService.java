package com.dhami.orderservice.order.service;

import com.dhami.orderservice.order.dto.CreateOrderRequest;
import com.dhami.orderservice.order.dto.UpdateOrderRequest;
import com.dhami.orderservice.order.dto.UpdateOrderStatusRequest;
import com.dhami.orderservice.order.dto.OrderResponse;
import com.dhami.orderservice.common.dto.PageResponse;
import com.dhami.orderservice.order.OrderStatus;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse retryPayment(Long id);

    OrderResponse confirmOrder(Long id);

    OrderResponse markPaymentFailed(Long id);

    OrderResponse getOrderById(Long id);

    PageResponse<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable);

    OrderResponse updateOrder(Long id, UpdateOrderRequest request);

    OrderResponse cancelOrder(Long id);

    OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request);

    PageResponse<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);
}
