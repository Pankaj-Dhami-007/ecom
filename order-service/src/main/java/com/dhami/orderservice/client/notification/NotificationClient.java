package com.dhami.orderservice.client.notification;

import com.dhami.orderservice.client.notification.dto.NotificationRequest;
import com.dhami.orderservice.client.notification.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST client used by order-service to push notifications into notification-service.
 *
 * ------------------------------------------------------------------------------------------
 * DEAD CODE as of the Kafka change. Only
 * {@link com.dhami.orderservice.client.notification.adapter.FeignOrderNotificationService}
 * referenced this client, and that adapter is no longer wired into OrderServiceImpl.
 *
 * Kept so the Feign wiring and the notification-service.url property stay valid, and so the
 * old approach remains readable.
 * ------------------------------------------------------------------------------------------
 */
@Deprecated(since = "1.0.0", forRemoval = true)
@FeignClient(name = "notification-service", url = "${notification-service.url}")
public interface NotificationClient {

    @PostMapping("/api/v1/notifications/order-event")
    NotificationResponse sendOrderNotification(@RequestBody NotificationRequest request);
}
