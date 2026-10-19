package com.dhami.notificationservice.kafka;

import com.dhami.notificationservice.dto.OrderEventType;
import com.dhami.notificationservice.dto.OrderNotificationRequest;
import com.dhami.notificationservice.kafka.dto.OrderCancelledEvent;
import com.dhami.notificationservice.kafka.dto.OrderCreatedEvent;
import com.dhami.notificationservice.kafka.dto.OrderDeliveredEvent;
import com.dhami.notificationservice.service.NotificationRecipientResolver;
import com.dhami.notificationservice.service.OrderEventNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderEventNotificationService orderEventNotificationService;
    private final NotificationRecipientResolver recipientResolver;

    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            // names the record to build for this topic; every topic carries one event type
            properties = "spring.json.value.default.type=com.dhami.notificationservice.kafka.dto.OrderCreatedEvent"
    )
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created | order id: {} | number: {} | event id: {}",
                event.orderId(), event.orderNumber(), event.eventId());

        sendOrderEmail(event.orderNumber(), event.customerId(), event.totalAmount(), OrderEventType.ORDER_CREATED);
    }

    @KafkaListener(
            topics = KafkaTopics.ORDER_CANCELLED,
            properties = "spring.json.value.default.type=com.dhami.notificationservice.kafka.dto.OrderCancelledEvent"
    )
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order.cancelled | order id: {} | number: {} | event id: {}",
                event.orderId(), event.orderNumber(), event.eventId());

        sendOrderEmail(event.orderNumber(), event.customerId(), event.totalAmount(), OrderEventType.ORDER_CANCELLED);
    }

    @KafkaListener(
            topics = KafkaTopics.ORDER_DELIVERED,
            properties = "spring.json.value.default.type=com.dhami.notificationservice.kafka.dto.OrderDeliveredEvent"
    )
    public void onOrderDelivered(OrderDeliveredEvent event) {
        log.info("Received order.delivered | order id: {} | number: {} | event id: {}",
                event.orderId(), event.orderNumber(), event.eventId());

        sendOrderEmail(event.orderNumber(), event.customerId(), event.totalAmount(), OrderEventType.ORDER_DELIVERED);
    }

    private void sendOrderEmail(String orderNumber, Long customerId, BigDecimal totalAmount, OrderEventType eventType) {
        try {
            var request = OrderNotificationRequest.builder()
                    .to(recipientResolver.resolveEmail(customerId))
                    .customerName(recipientResolver.resolveDisplayName(customerId))
                    .orderNumber(orderNumber)
                    .totalAmount(totalAmount)
                    .eventType(eventType)
                    .build();

            orderEventNotificationService.sendOrderNotification(request);
        } catch (Exception e) {
            log.error("Could not send {} email for order number: {} | customer id: {}",
                    eventType, orderNumber, customerId, e);
        }
    }
}