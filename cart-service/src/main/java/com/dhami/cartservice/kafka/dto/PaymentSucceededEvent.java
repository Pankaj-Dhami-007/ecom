package com.dhami.cartservice.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Received from payment-service when the gateway approved a charge.
 *
 * <p>This record is cart-service's own copy of the contract that payment-service publishes.
 * There is no shared event module in this project, so the shape is declared on both sides.
 * If the two ever drift, deserialisation fails loudly instead of quietly converting the
 * wrong cart.
 *
 * <p>Only {@code orderId} is used to do the work. It is the link back to the cart, because
 * the cart is the one that recorded which order the checkout produced. The other fields are
 * carried for logging.
 *
 * @param eventId          unique id of the producing event, for end to end tracing
 * @param orderId          the order that was paid for, and how the cart is found
 * @param customerId       the payer
 * @param paymentReference payment-service's own reference for the charge
 * @param amount           the amount charged
 * @param currency         ISO currency code of {@code amount}
 * @param occurredAt       when the charge succeeded, in UTC
 */
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