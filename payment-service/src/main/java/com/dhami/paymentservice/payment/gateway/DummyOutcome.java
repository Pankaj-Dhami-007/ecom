package com.dhami.paymentservice.payment.gateway;

/**
 * What the dummy gateway should do on the next charge.
 *
 * SUCCESS and FAILURE cover the two normal paths. TIMEOUT simulates a gateway that never
 * answers, which is the interesting case: the payment stays PENDING and the order keeps
 * its stock reserved instead of being released.
 */
public enum DummyOutcome {
    SUCCESS,
    FAILURE,
    TIMEOUT
}
