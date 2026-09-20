package com.dhami.ecom.order_service.order.service;

import com.dhami.ecom.order_service.order.dto.CreateOrderRequest;
import com.dhami.ecom.order_service.order.dto.UpdateOrderRequest;
import com.dhami.ecom.order_service.order.dto.UpdateOrderStatusRequest;
import com.dhami.ecom.order_service.order.dto.OrderResponse;
import com.dhami.ecom.order_service.common.dto.PageResponse;
import com.dhami.ecom.order_service.order.OrderStatus;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    PageResponse<OrderResponse> getAllOrders(Pageable pageable);

    PageResponse<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable);

    OrderResponse updateOrder(Long id, UpdateOrderRequest request);

    OrderResponse cancelOrder(Long id);

    OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request);

    PageResponse<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);
}
