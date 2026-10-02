package com.dhami.paymentservice.payment.service;

import com.dhami.paymentservice.payment.PaymentStatus;
import com.dhami.paymentservice.payment.dto.CreatePaymentRequest;
import com.dhami.paymentservice.payment.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentResponse getPaymentByOrderId(Long orderId);

    List<PaymentResponse> getPaymentsByCustomerId(Long customerId);
}
