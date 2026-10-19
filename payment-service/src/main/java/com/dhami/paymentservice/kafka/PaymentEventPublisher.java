package com.dhami.paymentservice.kafka;

import com.dhami.paymentservice.kafka.dto.PaymentFailedEvent;
import com.dhami.paymentservice.kafka.dto.PaymentSucceededEvent;
import com.dhami.paymentservice.payment.PaymentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;


/**
 * Publishes payment success and failure events to Kafka.
 *
 * 1. Creates payment event objects containing payment details.
 * 2. Publishes events to the respective Kafka topics using KafkaTemplate.
 * 3. Uses the order ID as the Kafka message key.
 * 4. Handles asynchronous send results by logging success or failure.
 * 5. Enables communication between payment-service and order-service
 *    without requiring a direct synchronous HTTP call.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    /*
     * KafkaTemplate is used to publish messages to Kafka topics.
     * 1. String represents the Kafka message key type.
     * 2. Object represents the message value type, allowing this template
     *    to send different event types, such as PaymentSucceededEvent
     *    and PaymentFailedEvent.
     * 3. The configured value serializer must support both event types.
     * 4. Using Object avoids the need for separate KafkaTemplate instances
     *    for each event type.
     */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSucceeded(PaymentEntity payment) {
        log.info("Preparing PAYMENT_SUCCEEDED event for payment ref: {} and order id: {}",
                payment.getPaymentReference(), payment.getOrderId());

        // Creates a PaymentSucceededEvent
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                generateEventId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getCurrency(),
                Instant.now()
        );

        // Publishes a payment success event to Kafka.
        send(KafkaTopics.PAYMENT_SUCCEEDED, payment.getOrderId(), event);
    }


    public void publishFailed(PaymentEntity payment) {
        log.info("Preparing PAYMENT_FAILED event for payment ref: {} and order id: {}",
                payment.getPaymentReference(), payment.getOrderId());

        PaymentFailedEvent event = new PaymentFailedEvent(
                generateEventId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getFailureReason(),
                Instant.now()
        );

        // // Publishes a payment failure event to Kafka.
        send(KafkaTopics.PAYMENT_FAILED, payment.getOrderId(), event);
    }

    // Sends an event to Kafka asynchronously
    // Uses KafkaTemplate.send() to publish the event to the specified topic.
    // Uses whenComplete() to handle successful and failed send operations.
    // Logs an error if publishing fails.
    // On success, retrieves RecordMetadata to log the topic, partition,
    // and offset of the published record.

    // Note: Kafka preserves record ordering within a partition, not across
    // different topics. Using the order ID as the key helps route events
    // for the same order to the same partition within a topic.
    private void send(String topic, Long orderId, Object event) {
        kafkaTemplate.send(topic, String.valueOf(orderId), event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Could not publish event to topic: {} for order id: {} - consumer will not see it",
                                topic, orderId, throwable);
                        return;
                    }
                    var metadata = result.getRecordMetadata();
                    log.info("Published event to topic: {} | partition: {} | offset: {} | order id: {}",
                            topic, metadata.partition(), metadata.offset(), orderId);
                });
    }

    // Generates a unique identifier for each event.
    // Helps identify and trace events across services through logs.
    private String generateEventId() {
        return UUID.randomUUID().toString();
    }
}