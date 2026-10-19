package com.dhami.orderservice.kafka;

import com.dhami.orderservice.kafka.dto.OrderCancelledEvent;
import com.dhami.orderservice.kafka.dto.OrderCreatedEvent;
import com.dhami.orderservice.kafka.dto.OrderDeliveredEvent;
import com.dhami.orderservice.order.OrderEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishCreated(OrderEntity order) {
        log.info("Raising ORDER_CREATED for order id: {} and number: {}",
                order.getId(), order.getOrderNumber());

        applicationEventPublisher.publishEvent(new OrderCreatedEvent(
                generateEventId(),
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getTotalAmount(),
                Instant.now()
        ));
    }

    public void publishCancelled(OrderEntity order) {
        log.info("Raising ORDER_CANCELLED for order id: {} and number: {}",
                order.getId(), order.getOrderNumber());

        applicationEventPublisher.publishEvent(new OrderCancelledEvent(
                generateEventId(),
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getTotalAmount(),
                Instant.now()
        ));
    }

    public void publishDelivered(OrderEntity order) {
        log.info("Raising ORDER_DELIVERED for order id: {} and number: {}",
                order.getId(), order.getOrderNumber());

        applicationEventPublisher.publishEvent(new OrderDeliveredEvent(
                generateEventId(),
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getTotalAmount(),
                Instant.now()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderCreated(OrderCreatedEvent event) {
        send(KafkaTopics.ORDER_CREATED, event.orderId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderCancelled(OrderCancelledEvent event) {
        send(KafkaTopics.ORDER_CANCELLED, event.orderId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderDelivered(OrderDeliveredEvent event) {
        send(KafkaTopics.ORDER_DELIVERED, event.orderId(), event);
    }

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

    private String generateEventId() {
        return UUID.randomUUID().toString();
    }
}