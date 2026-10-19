package com.dhami.notificationservice.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentSucceededEvent(
        String eventId,
        Long orderId,
        Long customerId,
        String paymentReference,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {
}