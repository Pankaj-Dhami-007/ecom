package com.dhami.paymentservice.client.order;

import com.dhami.paymentservice.client.order.dto.OrderResponse;
import com.dhami.paymentservice.common.dto.ApiResponse;
import com.dhami.paymentservice.constants.Routes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "order-service",
        url = "${order-service.url}",
        path = Routes.API_V1 + Routes.Orders.ROOT
)
public interface OrderClient {

    @PutMapping("/{id}/confirm")
    ApiResponse<OrderResponse> confirmOrder(
            @PathVariable("id") Long orderId,
            @RequestHeader("X-User-Id") Long customerId);

    @PutMapping("/{id}/payment-failed")
    ApiResponse<OrderResponse> markPaymentFailed(
            @PathVariable("id") Long orderId,
            @RequestHeader("X-User-Id") Long customerId);
}
