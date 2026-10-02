package com.dhami.paymentservice.payment.gateway;

/**
 * The seam between payment-service and whatever actually moves money.
 *
 * Today the only implementation is DummyPaymentGateway. Adding a real provider later
 * means writing one more class (RazorpayPaymentGateway) plus a signature/HTTP call and
 * flipping payment.gateway.type in config. Nothing in PaymentServiceImpl changes.
 */
public interface PaymentGateway {

    String name();

    PaymentGatewayResult charge(PaymentGatewayRequest request);
}
