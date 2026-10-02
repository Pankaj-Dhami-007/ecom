package com.dhami.notificationservice.service;

import com.dhami.notificationservice.dto.EmailRequest;
import com.dhami.notificationservice.dto.OrderEventType;
import com.dhami.notificationservice.dto.OrderNotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderEventNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OrderEventNotificationService.class);

    private static final Map<OrderEventType, String> SUBJECT_TEMPLATES = Map.of(
            OrderEventType.ORDER_CREATED, "Order %s - Created Successfully",
            OrderEventType.ORDER_CANCELLED, "Order %s - Cancelled",
            OrderEventType.ORDER_SHIPPED, "Order %s - Shipped",
            OrderEventType.ORDER_DELIVERED, "Order %s - Delivered"
    );

    private static final Map<OrderEventType, String> BODY_TEMPLATES = Map.of(
            OrderEventType.ORDER_CREATED, """
                    Dear %s,

                    Your order %s has been created successfully.

                    Order Number: %s
                    Total Amount: $%.2f

                    We will notify you once your order is confirmed and shipped.

                    Thank you for shopping with us!
                    """,

            OrderEventType.ORDER_CANCELLED, """
                    Dear %s,

                    Your order %s has been cancelled.

                    Order Number: %s
                    Total Amount: $%.2f

                    If you have any questions, please contact our support team.

                    Thank you.
                    """,

            OrderEventType.ORDER_SHIPPED, """
                    Dear %s,

                    Your order %s has been shipped!

                    Order Number: %s
                    Total Amount: $%.2f

                    You will receive your order soon.

                    Thank you for shopping with us!
                    """,

            OrderEventType.ORDER_DELIVERED, """
                    Dear %s,

                    Your order %s has been delivered successfully.

                    Order Number: %s
                    Total Amount: $%.2f

                    We hope you enjoy your purchase!

                    Thank you for shopping with us!
                    """
    );

    private final EmailService emailService;

    public OrderEventNotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendOrderNotification(OrderNotificationRequest request) {
        log.info("Processing order notification for event: {} | order: {} | customer: {}",
                request.getEventType(), request.getOrderNumber(), request.getCustomerName());

        var subject = buildSubject(request);
        var body = buildBody(request);

        var emailRequest = EmailRequest.builder()
                .to(request.getTo())
                .subject(subject)
                .body(body)
                .html(false)
                .build();

        emailService.sendEmail(emailRequest);

        log.info("Order notification sent successfully for event: {} | order: {}",
                request.getEventType(), request.getOrderNumber());
    }

    private String buildSubject(OrderNotificationRequest request) {
        var template = SUBJECT_TEMPLATES.get(request.getEventType());
        if (template == null) {
            log.warn("No subject template found for event type: {}", request.getEventType());
            return "Order " + request.getOrderNumber() + " - Update";
        }
        return String.format(template, request.getOrderNumber());
    }

    private String buildBody(OrderNotificationRequest request) {
        var template = BODY_TEMPLATES.get(request.getEventType());
        if (template == null) {
            log.warn("No body template found for event type: {}", request.getEventType());
            return "Your order " + request.getOrderNumber() + " has been updated.";
        }
        return String.format(template,
                request.getCustomerName(),
                request.getOrderNumber(),
                request.getOrderNumber(),
                request.getTotalAmount());
    }
}
