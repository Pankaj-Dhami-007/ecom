package com.dhami.paymentservice.payment.service;

import com.dhami.paymentservice.client.order.OrderClient;
import com.dhami.paymentservice.payment.PaymentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Pushes the payment result back to the service that owns the order.
 *
 * Payment-service deliberately does not call cart-service. Checkout runs as a chain
 * (cart -> order -> payment), and calling back into cart from here re-enters the cart
 * transaction that is still waiting on this very call, so the cart could not see its own
 * committed state. The cart re-reads the order status itself once checkout returns and
 * marks itself CONVERTED, which keeps each service responsible for its own data.
 *
 * This is the synchronous stand-in for the PAYMENT_SUCCESS / PAYMENT_FAILED events. When
 * an event bus is introduced, a listener calls the same order endpoints and this class
 * disappears - the contract does not change.
 *
 * The call is guarded: a payment is already recorded as SUCCESS in our own database
 * before this runs, so a downstream failure must not undo that. It is logged loudly and
 * left for reconciliation instead.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackService {

    private final OrderClient orderClient;

    public void onPaymentSuccess(PaymentEntity payment) {
        log.info("Payment {} succeeded - confirming order id: {}",
                payment.getPaymentReference(), payment.getOrderId());

        try {
            orderClient.confirmOrder(payment.getOrderId(), payment.getCustomerId());
            log.info("Order {} confirmed and reserved stock marked as sold", payment.getOrderId());
        } catch (Exception e) {
            log.error("Payment {} succeeded but order {} could not be confirmed - manual reconciliation needed",
                    payment.getPaymentReference(), payment.getOrderId(), e);
        }
    }

    public void onPaymentFailed(PaymentEntity payment) {
        log.info("Payment {} failed - notifying order service for order id: {}",
                payment.getPaymentReference(), payment.getOrderId());

        try {
            orderClient.markPaymentFailed(payment.getOrderId(), payment.getCustomerId());
            log.info("Order {} moved to PAYMENT_FAILED and reserved stock released", payment.getOrderId());
        } catch (Exception e) {
            log.error("Payment {} failed but order {} could not be updated - manual reconciliation needed",
                    payment.getPaymentReference(), payment.getOrderId(), e);
        }
    }
}
