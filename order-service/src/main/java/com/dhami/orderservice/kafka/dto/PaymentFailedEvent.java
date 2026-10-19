package com.dhami.orderservice.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentFailedEvent(
        String eventId,
        Long orderId,
        Long customerId,
        String paymentReference,
        BigDecimal amount,
        String currency,
        String failureReason,
        Instant occurredAt
) {
}