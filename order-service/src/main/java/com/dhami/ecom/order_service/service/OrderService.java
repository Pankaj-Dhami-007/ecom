package com.dhami.ecom.order_service.service;

import com.dhami.ecom.order_service.dto.request.CreateOrderRequest;
import com.dhami.ecom.order_service.dto.request.UpdateOrderRequest;
import com.dhami.ecom.order_service.dto.request.UpdateOrderStatusRequest;
import com.dhami.ecom.order_service.dto.response.OrderResponse;
import com.dhami.ecom.order_service.dto.response.PageResponse;
import com.dhami.ecom.order_service.entity.OrderStatus;
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
