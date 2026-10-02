package com.dhami.orderservice.client.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String paymentId,
        Long orderId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        String message,
        Instant createdAt
) {
}
