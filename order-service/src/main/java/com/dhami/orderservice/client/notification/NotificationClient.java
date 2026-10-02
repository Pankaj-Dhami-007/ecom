package com.dhami.orderservice.client.notification;

import com.dhami.orderservice.client.notification.dto.NotificationRequest;
import com.dhami.orderservice.client.notification.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification-service.url}")
public interface NotificationClient {

    @PostMapping("/api/v1/notifications/order-event")
    NotificationResponse sendOrderNotification(@RequestBody NotificationRequest request);
}
