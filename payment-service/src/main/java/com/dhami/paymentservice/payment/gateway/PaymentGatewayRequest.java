package com.dhami.paymentservice.payment.gateway;

import java.math.BigDecimal;

/**
 * Gateway-facing view of a payment. Kept separate from PaymentEntity on purpose so the
 * gateway never sees our database ids, timestamps or callback plumbing.
 */
public record PaymentGatewayRequest(
        String paymentReference,
        Long orderId,
        BigDecimal amount,
        String currency
) {
}
