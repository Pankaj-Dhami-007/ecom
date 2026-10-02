package com.dhami.paymentservice.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String paymentId,
        Long orderId,
        Long cartId,
        String status,
        BigDecimal amount,
        String currency,
        String gateway,
        String gatewayReference,
        String message,
        Instant createdAt
) {
}
