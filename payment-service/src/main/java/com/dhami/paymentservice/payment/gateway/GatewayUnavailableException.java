package com.dhami.paymentservice.payment.gateway;

/**
 * Thrown when a gateway cannot be reached or does not answer in time.
 *
 * This is deliberately different from PaymentGatewayResult.declined(...): the caller
 * must not treat an unknown outcome as a failure, because the money may still be moving.
 */
public class GatewayUnavailableException extends RuntimeException {

    public GatewayUnavailableException(String message) {
        super(message);
    }
}
