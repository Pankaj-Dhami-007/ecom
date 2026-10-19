package com.dhami.paymentservice.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * This service's own copy of the order-created record published by order-service.
 *
 * <p>The producer writes its own class name into the Jackson type header, so a consumer can
 * only ever bind to a type it actually ships. Each service therefore keeps its own record
 * and {@code spring.json.value.use.type.headers=false} makes the listener map the JSON onto
 * this one instead of chasing the producer's name.
 *
 * <p>The fields mirror {@code com.dhami.orderservice.kafka.dto.OrderCreatedEvent}. Adding a
 * field is safe, renaming or retyping one is a breaking change for this consumer.
 *
 * @param orderId     the order to charge
 * @param orderNumber customer facing order number, kept for logs and traceability
 * @param customerId  the buyer
 * @param totalAmount amount to charge
 * @param occurredAt  when the order was created, in UTC
 */
public record OrderCreatedEvent(
        Long orderId,
        String orderNumber,
        Long customerId,
        BigDecimal totalAmount,
        Instant occurredAt
) {
}