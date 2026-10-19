package com.dhami.orderservice.kafka;

import com.dhami.orderservice.kafka.dto.PaymentFailedEvent;
import com.dhami.orderservice.kafka.dto.PaymentSucceededEvent;
import com.dhami.orderservice.order.OrderServiceException;
import com.dhami.orderservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderService orderService;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCEEDED,
            // tells the JSON deserializer which record to build for this topic. Each topic
            // carries exactly one event type, so no polymorphic handling is needed.
            properties = "spring.json.value.default.type=com.dhami.orderservice.kafka.dto.PaymentSucceededEvent"
    )
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Received PAYMENT_SUCCEEDED for order id: {} | payment ref: {} | event id: {}",
                event.orderId(), event.paymentReference(), event.eventId());

        applyPaymentOutcome(event.orderId(), event.eventId(),
                () -> orderService.confirmOrder(event.orderId()));
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            // tells the JSON deserializer which record to build for this topic
            properties = "spring.json.value.default.type=com.dhami.orderservice.kafka.dto.PaymentFailedEvent"
    )
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Received PAYMENT_FAILED for order id: {} | payment ref: {} | reason: {} | event id: {}",
                event.orderId(), event.paymentReference(), event.failureReason(), event.eventId());

        applyPaymentOutcome(event.orderId(), event.eventId(),
                () -> orderService.markPaymentFailed(event.orderId()));
    }

    private void applyPaymentOutcome(Long orderId, String eventId, Runnable statusChange) {
        try {
            statusChange.run();
            log.info("Applied payment outcome for order id: {} | event id: {}", orderId, eventId);
        } catch (OrderServiceException e) {
            log.warn("Payment outcome for order id: {} was rejected by the order state machine - acknowledging | event id: {} | reason: {}",
                    orderId, eventId, e.getMessage());
        }
    }
}