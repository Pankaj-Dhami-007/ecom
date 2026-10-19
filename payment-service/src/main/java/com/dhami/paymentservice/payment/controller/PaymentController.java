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

        PaymentService.CreatePaymentResult result = paymentService.createPayment(request);
        log.info("Payment attempt for order id: {} finished with status {} (charged: {})",
                request.orderId(), result.status(), result.charged());

        // The stored payment is returned rather than the result record, because this endpoint
        // is still reachable directly for testing and for a future caller that is not the
        // order flow. The row is the truth about the attempt; an unanswered gateway leaves it
        // PENDING, which the caller can see here and act on.
        return ResponseEntity.ok(
                ApiResponse.success(paymentService.getPaymentByOrderId(request.orderId()),
                        result.message())
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
