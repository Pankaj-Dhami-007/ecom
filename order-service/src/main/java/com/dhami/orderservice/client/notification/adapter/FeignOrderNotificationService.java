package com.dhami.orderservice.client.notification.adapter;

import com.dhami.orderservice.client.notification.NotificationClient;
import com.dhami.orderservice.client.notification.dto.NotificationRequest;
import com.dhami.orderservice.order.OrderEntity;
import com.dhami.orderservice.order.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Delivers order notifications to notification-service over HTTP.
 *
 * ------------------------------------------------------------------------------------------
 * DEAD CODE as of the Kafka change. OrderServiceImpl no longer injects this adapter; it
 * publishes on Kafka through
 * {@link com.dhami.orderservice.kafka.OrderEventPublisher} and notification-service consumes
 * the events in its own listeners.
 *
 * Why that matters: this path ran inside the order transaction, so every order operation
 * waited on an SMTP round trip, and a mail outage could fail an order that was perfectly
 * valid. Note also the hardcoded recipient below, which exists only because order-service
 * never had the customer's email. On the Kafka path the event carries the customer id and
 * notification-service resolves the recipient on its own side.
 *
 * Kept as dead code on purpose, so the old approach stays readable and reversible.
 * ------------------------------------------------------------------------------------------
 */
@Deprecated(since = "1.0.0", forRemoval = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class FeignOrderNotificationService implements OrderNotificationService {

    private final NotificationClient notificationClient;

    @Override
    public void onOrderCreated(OrderEntity order) {
        log.info("Sending ORDER_CREATED notification for order id: {}", order.getId());
        sendNotification(order, "ORDER_CREATED");
    }

    @Override
    public void onOrderCancelled(OrderEntity order) {
        log.info("Sending ORDER_CANCELLED notification for order id: {}", order.getId());
        sendNotification(order, "ORDER_CANCELLED");
    }

    @Override
    public void onOrderDelivered(OrderEntity order) {
        log.info("Sending ORDER_DELIVERED notification for order id: {}", order.getId());
        sendNotification(order, "ORDER_DELIVERED");
    }

    private void sendNotification(OrderEntity order, String eventType) {
        try {
            var request = NotificationRequest.builder()
                    .to("pankajdhami357@gmail.com")
                    //.to("customer-" + order.getCustomerId() + "@email.com")
                    .customerName("Customer " + order.getCustomerId())
                    .orderNumber(order.getOrderNumber())
                    .totalAmount(order.getTotalAmount())
                    .eventType(eventType)
                    .build();

            var response = notificationClient.sendOrderNotification(request);

            if (response.isSuccess()) {
                log.info("Notification sent successfully for order {} | event: {}", order.getOrderNumber(), eventType);
            } else {
                log.warn("Notification service returned failure for order {} | event: {} | message: {}",
                        order.getOrderNumber(), eventType, response.getMessage());
            }
        } catch (Exception ex) {
            log.error("Failed to send notification for order {} | event: {}",
                    order.getOrderNumber(), eventType, ex);
        }
    }
}
