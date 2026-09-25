package com.dhami.ecom.order_service.order;

import com.dhami.ecom.order_service.constants.Routes;
import com.dhami.ecom.order_service.common.context.RequestUserContext;
import com.dhami.ecom.order_service.order.dto.CreateOrderRequest;
import com.dhami.ecom.order_service.order.dto.UpdateOrderRequest;
import com.dhami.ecom.order_service.order.dto.UpdateOrderStatusRequest;
import com.dhami.ecom.order_service.common.dto.ApiResponse;
import com.dhami.ecom.order_service.order.dto.OrderResponse;
import com.dhami.ecom.order_service.common.dto.PageResponse;
import com.dhami.ecom.order_service.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1 + Routes.Orders.ROOT)
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Create order request received with {} item(s)", request.getItems().size());
        OrderResponse response = orderService.createOrder(request);
        log.info("Order created successfully with id: {} and number: {}", response.getId(), response.getOrderNumber());
        return new ResponseEntity<>(
                ApiResponse.success(response, "Order created successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping(Routes.Orders.ID)
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        log.info("Fetching order by id: {}", id);
        return ResponseEntity.ok(
                ApiResponse.success(orderService.getOrderById(id), "Order retrieved successfully")
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (status != null) {
            log.info("Fetching orders by status: {} - page: {}, size: {}", status, pageable.getPageNumber(), pageable.getPageSize());
            return ResponseEntity.ok(
                    ApiResponse.success(
                            orderService.getOrdersByStatus(status, pageable),
                            "Orders retrieved successfully"
                    )
            );
        }
        Long userId = RequestUserContext.requireUserId();
        log.info("Fetching orders for customer id: {} - page: {}, size: {}", userId, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.getOrdersByCustomerId(userId, pageable),
                        "Orders retrieved successfully"
                )
        );
    }

    @PutMapping(Routes.Orders.ID)
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {
        log.info("Update order request received for order id: {}", id);
        return ResponseEntity.ok(
                ApiResponse.success(orderService.updateOrder(id, request), "Order updated successfully")
        );
    }

    @PutMapping(Routes.Orders.ID_CANCEL)
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        log.info("Cancel order request received for order id: {}", id);
        OrderResponse response = orderService.cancelOrder(id);
        log.info("Order cancelled successfully with id: {}", id);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Order cancelled successfully")
        );
    }

    @PatchMapping(Routes.Orders.ID_STATUS)
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("Update order status request received for order id: {} to {}", id, request.getStatus());
        OrderResponse response = orderService.updateOrderStatus(id, request);
        log.info("Order status updated successfully for id: {} to {}", id, response.getStatus());
        return ResponseEntity.ok(
                ApiResponse.success(response, "Order status updated successfully")
        );
    }
}
