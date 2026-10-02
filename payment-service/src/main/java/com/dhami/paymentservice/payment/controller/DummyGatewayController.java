package com.dhami.paymentservice.payment.controller;

import com.dhami.paymentservice.common.dto.ApiResponse;
import com.dhami.paymentservice.constants.Routes;
import com.dhami.paymentservice.payment.dto.DummyGatewayOutcomeResponse;
import com.dhami.paymentservice.payment.gateway.DummyOutcome;
import com.dhami.paymentservice.payment.gateway.DummyPaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only helper for driving the dummy gateway. Lets you flip the next charge between
 * SUCCESS, FAILURE and TIMEOUT without a restart, so all three branches of the
 * order/cart flow can be demonstrated.
 *
 * It talks to DummyPaymentGateway directly and is guarded by the same condition, so it
 * disappears together with the dummy gateway. Nothing in the real payment API knows this
 * class exists, which is what keeps the gateway swap clean.
 */
@RestController
@ConditionalOnProperty(name = "payment.gateway.type", havingValue = "dummy", matchIfMissing = true)
@RequestMapping(Routes.API_V1 + Routes.Payments.ROOT + Routes.Payments.GATEWAY_OUTCOME)
@RequiredArgsConstructor
@Slf4j
public class DummyGatewayController {

    private final DummyPaymentGateway dummyPaymentGateway;

    @GetMapping
    public ResponseEntity<ApiResponse<DummyGatewayOutcomeResponse>> getOutcome() {
        DummyOutcome outcome = dummyPaymentGateway.currentOutcome();
        return ResponseEntity.ok(
                ApiResponse.success(new DummyGatewayOutcomeResponse(outcome), "Current dummy outcome")
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<DummyGatewayOutcomeResponse>> setOutcome(@RequestParam DummyOutcome outcome) {
        log.info("Setting dummy gateway outcome to: {}", outcome);
        DummyOutcome updated = dummyPaymentGateway.changeOutcome(outcome);
        return ResponseEntity.ok(
                ApiResponse.success(new DummyGatewayOutcomeResponse(updated), "Dummy outcome updated")
        );
    }
}
