package com.dhami.ecom.order_service.client.notification.adapter;

import com.dhami.ecom.order_service.client.notification.NotificationClient;
import com.dhami.ecom.order_service.client.notification.dto.NotificationRequest;
import com.dhami.ecom.order_service.order.OrderEntity;
import com.dhami.ecom.order_service.order.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
