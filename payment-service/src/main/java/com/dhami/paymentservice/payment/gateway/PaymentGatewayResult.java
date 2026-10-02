package com.dhami.paymentservice.payment.gateway;

/**
 * Outcome of a gateway charge.
 *
 * A decline is a normal result. An unreachable gateway is not represented here at all -
 * it throws, because "we never heard back" is a different situation from "the customer
 * was refused" and the two need different handling.
 */
public record PaymentGatewayResult(
        boolean approved,
        String gatewayReference,
        String failureReason
) {
    public static PaymentGatewayResult approved(String gatewayReference) {
        return new PaymentGatewayResult(true, gatewayReference, null);
    }

    public static PaymentGatewayResult declined(String failureReason) {
        return new PaymentGatewayResult(false, null, failureReason);
    }
}
