package com.dhami.paymentservice.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Stand-in for a real payment provider. It approves or declines instantly and returns a
 * fake transaction id, so the whole order/cart flow can be exercised end to end without
 * credentials, webhooks or network calls.
 *
 * The outcome is mutable at runtime (see DummyGatewayController) so both the success and
 * the failure branch can be demonstrated without restarting the service.
 */
@Component
@ConditionalOnProperty(name = "payment.gateway.type", havingValue = "dummy", matchIfMissing = true)
@Slf4j
public class DummyPaymentGateway implements PaymentGateway {

    private static final String GATEWAY_NAME = "dummy";

    private final AtomicReference<DummyOutcome> currentOutcome;

    public DummyPaymentGateway(
            @Value("${payment.gateway.dummy.default-outcome:SUCCESS}") DummyOutcome defaultOutcome) {
        this.currentOutcome = new AtomicReference<>(defaultOutcome);
        log.info("Dummy payment gateway active with default outcome: {}", defaultOutcome);
    }

    @Override
    public String name() {
        return GATEWAY_NAME;
    }

    @Override
    public PaymentGatewayResult charge(PaymentGatewayRequest request) {
        DummyOutcome outcome = currentOutcome.get();
        log.info("[dummy-gateway] charging payment {} for order {} amount {} {} using outcome {}",
                request.paymentReference(), request.orderId(), request.amount(), request.currency(), outcome);

        return switch (outcome) {
            case SUCCESS -> PaymentGatewayResult.approved(generateReference());
            case FAILURE -> PaymentGatewayResult.declined("Payment declined by dummy gateway (simulated)");
            case TIMEOUT -> throw new GatewayUnavailableException("Dummy gateway did not respond (simulated timeout)");
        };
    }

    public DummyOutcome currentOutcome() {
        return currentOutcome.get();
    }

    public DummyOutcome changeOutcome(DummyOutcome outcome) {
        DummyOutcome previous = currentOutcome.getAndSet(outcome);
        log.info("[dummy-gateway] outcome changed from {} to {}", previous, outcome);
        return outcome;
    }

    private String generateReference() {
        return "DUMMY_TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }
}
