package com.dhami.paymentservice.payment.controller;

import com.dhami.paymentservice.common.dto.ApiResponse;
import com.dhami.paymentservice.constants.Routes;
import com.dhami.paymentservice.payment.dto.CreatePaymentRequest;
import com.dhami.paymentservice.payment.dto.PaymentResponse;
import com.dhami.paymentservice.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1 + Routes.Payments.ROOT)
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("Create payment request received for order id: {}", request.orderId());
        PaymentResponse payment = paymentService.createPayment(request);
        log.info("Payment {} finished with status {}", payment.paymentId(), payment.status());
        return ResponseEntity.ok(
                ApiResponse.success(payment, "Payment processed")
        );
    }

    @GetMapping(Routes.Payments.ORDER_ID)
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable Long orderId) {
        log.info("Get payment request received for order id: {}", orderId);
        return ResponseEntity.ok(
                ApiResponse.success(paymentService.getPaymentByOrderId(orderId), "Payment retrieved successfully")
        );
    }
}
