package com.dhami.notificationservice.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        String eventId,
        Long orderId,
        String orderNumber,
        Long customerId,
        BigDecimal totalAmount,
        Instant occurredAt
) {
}