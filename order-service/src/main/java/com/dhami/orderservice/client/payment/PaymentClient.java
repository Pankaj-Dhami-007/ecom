package com.dhami.orderservice.client.payment;

import com.dhami.orderservice.client.payment.dto.CreatePaymentRequest;
import com.dhami.orderservice.client.payment.dto.PaymentResponse;
import com.dhami.orderservice.common.dto.ApiResponse;
import com.dhami.orderservice.constants.Routes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "payment-service",
        url = "${payment-service.url}",
        path = Routes.API_V1 + Routes.Payments.ROOT
)
public interface PaymentClient {

    @PostMapping
    ApiResponse<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request);

    @GetMapping("/order/{orderId}")
    ApiResponse<PaymentResponse> getPaymentByOrderId(@PathVariable("orderId") Long orderId);
}
