package com.dhami.paymentservice.payment.service;

import com.dhami.paymentservice.payment.PaymentStatus;
import com.dhami.paymentservice.payment.dto.CreatePaymentRequest;
import com.dhami.paymentservice.payment.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {

    CreatePaymentResult createPayment(CreatePaymentRequest request);
    PaymentResponse getPaymentByOrderId(Long orderId);
    List<PaymentResponse> getPaymentsByCustomerId(Long customerId);

    record CreatePaymentResult(
            boolean charged,
            PaymentStatus status,
            String message
    ) {
    }
}