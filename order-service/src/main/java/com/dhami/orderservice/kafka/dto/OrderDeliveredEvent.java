package com.dhami.orderservice.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderDeliveredEvent(
        String eventId,
        Long orderId,
        String orderNumber,
        Long customerId,
        BigDecimal totalAmount,
        Instant occurredAt
) {
}