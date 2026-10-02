package com.dhami.orderservice.order.event;

/**
 * Published from inside the order transaction once the order row exists and its stock is
 * reserved. Carries ids only, never the entity: the listener runs after commit and must
 * read the freshly committed state itself.
 */
public record OrderPaymentRequestedEvent(
        Long orderId,
        String orderNumber
) {
}
