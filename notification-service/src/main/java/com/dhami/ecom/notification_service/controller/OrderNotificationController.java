package com.dhami.ecom.notification_service.controller;

import com.dhami.ecom.notification_service.dto.EmailResponse;
import com.dhami.ecom.notification_service.dto.OrderNotificationRequest;
import com.dhami.ecom.notification_service.service.OrderEventNotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/notifications")
public class OrderNotificationController {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationController.class);

    private final OrderEventNotificationService orderEventNotificationService;

    public OrderNotificationController(OrderEventNotificationService orderEventNotificationService) {
        this.orderEventNotificationService = orderEventNotificationService;
    }

    @PostMapping("/order-event")
    public ResponseEntity<EmailResponse> sendOrderNotification(@Valid @RequestBody OrderNotificationRequest request) {
        log.info("REST request to send order notification for event: {} | order: {}",
                request.getEventType(), request.getOrderNumber());

        orderEventNotificationService.sendOrderNotification(request);

        var response = EmailResponse.builder()
                .success(true)
                .message(request.getEventType() + " notification sent successfully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
