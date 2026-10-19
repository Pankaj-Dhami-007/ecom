package com.dhami.paymentservice.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Published after the gateway has declined a charge.
 *
 * <p>Declared as a record because an event is an immutable fact, not a mutable object. A
 * record also gives equality, a readable toString and Jackson deserialisation support for
 * free, which removes a whole category of "why is this event not binding" bugs.
 *
 * <p>This is a public contract. Changing a field name or its meaning is a breaking change
 * for every consumer, so add new fields instead of renaming existing ones.
 *
 * @param eventId          unique id for this single event, used to trace one message end to end
 * @param orderId          the order the charge belongs to, and the Kafka message key so all
 *                         events for one order stay in order on the same partition
 * @param customerId       the payer, so consumers can address them without calling back here
 * @param paymentReference our own payment reference, stable across gateway swaps
 * @param amount           the amount that was attempted
 * @param currency         ISO currency code of {@code amount}
 * @param failureReason    gateway supplied explanation, safe to show to a customer
 * @param occurredAt       when this service recorded the failure, in UTC
 */
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