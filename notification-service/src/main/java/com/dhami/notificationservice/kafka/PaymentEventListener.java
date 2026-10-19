package com.dhami.notificationservice.kafka;

import com.dhami.notificationservice.dto.PaymentEventType;
import com.dhami.notificationservice.dto.PaymentNotificationRequest;
import com.dhami.notificationservice.kafka.dto.PaymentFailedEvent;
import com.dhami.notificationservice.kafka.dto.PaymentSucceededEvent;
import com.dhami.notificationservice.service.NotificationRecipientResolver;
import com.dhami.notificationservice.service.PaymentEventNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentEventNotificationService paymentEventNotificationService;
    private final NotificationRecipientResolver recipientResolver;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCEEDED,
            // names the record to build for this topic; every topic carries one event type
            properties = "spring.json.value.default.type=com.dhami.notificationservice.kafka.dto.PaymentSucceededEvent"
    )
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Received payment.succeeded | order id: {} | payment ref: {} | event id: {}",
                event.orderId(), event.paymentReference(), event.eventId());

        sendPaymentEmail(
                event.orderId(),
                event.customerId(),
                event.paymentReference(),
                event.amount(),
                event.currency(),
                null,
                PaymentEventType.PAYMENT_SUCCEEDED
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            properties = "spring.json.value.default.type=com.dhami.notificationservice.kafka.dto.PaymentFailedEvent"
    )
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Received payment.failed | order id: {} | payment ref: {} | reason: {} | event id: {}",
                event.orderId(), event.paymentReference(), event.failureReason(), event.eventId());

        sendPaymentEmail(
                event.orderId(),
                event.customerId(),
                event.paymentReference(),
                event.amount(),
                event.currency(),
                event.failureReason(),
                PaymentEventType.PAYMENT_FAILED
        );
    }

    private void sendPaymentEmail(Long orderId,
                                  Long customerId,
                                  String paymentReference,
                                  BigDecimal amount,
                                  String currency,
                                  String failureReason,
                                  PaymentEventType eventType) {
        try {
            var request = PaymentNotificationRequest.builder()
                    .to(recipientResolver.resolveEmail(customerId))
                    .customerName(recipientResolver.resolveDisplayName(customerId))
                    .orderNumber("order id " + orderId)
                    .paymentReference(paymentReference)
                    .amount(amount)
                    .currency(currency)
                    .failureReason(failureReason)
                    .eventType(eventType)
                    .build();

            paymentEventNotificationService.sendPaymentNotification(request);
        } catch (Exception e) {
            log.error("Could not send {} email | payment ref: {} | order id: {} | customer id: {}",
                    eventType, paymentReference, orderId, customerId, e);
        }
    }
}