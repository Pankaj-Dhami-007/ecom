package com.dhami.orderservice.order.event;

import com.dhami.orderservice.order.repository.OrderRepository;
import com.dhami.orderservice.order.service.OrderPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Starts the payment once the order transaction has actually committed.
 *
 * The payment must not be called from inside the order transaction. payment-service
 * calls straight back into order-service to confirm the order, and that callback runs on
 * a different connection: an uncommitted order row is invisible to it, so confirmOrder
 * would fail with 404 and the order would stay PENDING_PAYMENT forever.
 *
 * Running after commit fixes that and buys rollback safety for free. If the order
 * transaction rolls back, this listener never fires, so a payment can never be taken for
 * an order that does not exist.
 *
 * This is a plain in-process callback, not event-driven architecture. There is no broker
 * and no network hop - when a real broker is introduced, a consumer calls the same
 * orderPaymentService.initiatePayment(...) from the message handler instead.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentEventListener {

    private final OrderRepository orderRepository;
    private final OrderPaymentService orderPaymentService;

    // @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentRequested(OrderPaymentRequestedEvent event) {
        log.info("Order {} (id: {}) committed - starting payment",
                event.orderNumber(), event.orderId());

        // re-read instead of trusting a payload entity, so the committed row is what gets charged
        orderRepository.findById(event.orderId()).ifPresentOrElse(
                order -> orderPaymentService.initiatePayment(order),
                () -> log.error("Order id: {} not found after commit - skipping payment", event.orderId())
        );
    }
}

/*

Order create
    ↓
Order DB mein save
    ↓
Transaction COMMIT
    ↓
Spring automatically listener ko call karta hai
    ↓
onPaymentRequested(...)
    ↓
Payment Service ko call
 */
