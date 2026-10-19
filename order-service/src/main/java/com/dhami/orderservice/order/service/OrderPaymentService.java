package com.dhami.orderservice.order.service;

import com.dhami.orderservice.client.payment.PaymentClient;
import com.dhami.orderservice.client.payment.dto.CreatePaymentRequest;
import com.dhami.orderservice.order.OrderEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentService {

    private static final String DEFAULT_CURRENCY = "INR";

    private final PaymentClient paymentClient;

    public void initiatePayment(OrderEntity order) {
        log.info("Initiating payment for order id: {} with amount: {}", order.getId(), order.getTotalAmount());

        CreatePaymentRequest request = new CreatePaymentRequest(
                order.getId(),
                order.getCustomerId(),
                order.getCartId(),
                order.getTotalAmount(),
                DEFAULT_CURRENCY
        );

        try {
            paymentClient.createPayment(request);
            log.info("Payment initiated for order id: {}", order.getId());
        } catch (Exception e) {
            // order stays PENDING_PAYMENT with stock held in reserved, so the
            // payment can be retried later without losing the reservation
            log.warn("Could not initiate payment for order id: {} - order stays PENDING_PAYMENT", order.getId(), e);
        }
    }
}
