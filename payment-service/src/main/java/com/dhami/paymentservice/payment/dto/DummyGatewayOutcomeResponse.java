package com.dhami.paymentservice.payment.dto;

import com.dhami.paymentservice.payment.gateway.DummyOutcome;

/**
 * Test affordance for the dummy gateway. Delete this record together with
 * DummyGatewayController when a real gateway replaces the dummy one.
 */
public record DummyGatewayOutcomeResponse(
        DummyOutcome outcome
) {
}
