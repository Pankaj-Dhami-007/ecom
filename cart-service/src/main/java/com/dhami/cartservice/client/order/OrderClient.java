package com.dhami.cartservice.client.order;

import com.dhami.cartservice.client.order.dto.CreateOrderRequest;
import com.dhami.cartservice.client.order.dto.OrderResponse;
import com.dhami.cartservice.common.dto.ApiResponse;
import com.dhami.cartservice.constants.Routes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "order-service",
        url = "${order-service.url}",
        path = Routes.API_V1 + Routes.Orders.ROOT,
        configuration = OrderClientConfig.class
)
public interface OrderClient {

    @PostMapping
    ApiResponse<OrderResponse> createOrder(CreateOrderRequest request);

    @GetMapping(Routes.Orders.ID)
    ApiResponse<OrderResponse> getOrderById(@PathVariable("id") Long id);
}
