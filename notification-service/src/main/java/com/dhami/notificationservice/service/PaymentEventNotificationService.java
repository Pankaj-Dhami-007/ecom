package com.dhami.notificationservice.service;

import com.dhami.notificationservice.dto.EmailRequest;
import com.dhami.notificationservice.dto.PaymentEventType;
import com.dhami.notificationservice.dto.PaymentNotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class PaymentEventNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventNotificationService.class);

    private static final Map<PaymentEventType, String> SUBJECT_TEMPLATES = Map.of(
            PaymentEventType.PAYMENT_SUCCEEDED, "Payment %s - Successful",
            PaymentEventType.PAYMENT_FAILED, "Payment %s - Failed"
    );

    private static final Map<PaymentEventType, String> BODY_TEMPLATES = Map.of(
            PaymentEventType.PAYMENT_SUCCEEDED, """
                    Dear %s,

                    We have received your payment of %s %.2f.

                    Payment Reference: %s
                    Order Number: %s

                    Your order is now being processed.

                    Thank you for shopping with us!
                    """,

            PaymentEventType.PAYMENT_FAILED, """
                    Dear %s,

                    We could not collect your payment of %s %.2f.

                    Payment Reference: %s
                    Order Number: %s
                    Reason: %s

                    You can retry the payment for this order, and nothing has been charged.

                    Thank you.
                    """
    );

    private final EmailService emailService;

    public PaymentEventNotificationService(EmailService emailService) {
        this.emailService = emailService;
    }


    public void sendPaymentNotification(PaymentNotificationRequest request) {
        log.info("Processing payment notification for event: {} | payment ref: {} | customer: {}",
                request.getEventType(), request.getPaymentReference(), request.getCustomerName());

        var emailRequest = EmailRequest.builder()
                .to(request.getTo())
                .subject(buildSubject(request))
                .body(buildBody(request))
                .html(false)
                .build();

        emailService.sendEmail(emailRequest);

        log.info("Payment notification sent successfully for event: {} | payment ref: {}",
                request.getEventType(), request.getPaymentReference());
    }

    private String buildSubject(PaymentNotificationRequest request) {
        var template = SUBJECT_TEMPLATES.get(request.getEventType());
        if (template == null) {
            log.warn("No subject template found for payment event type: {}", request.getEventType());
            return "Payment " + request.getPaymentReference() + " - Update";
        }
        return String.format(template, request.getPaymentReference());
    }

    private String buildBody(PaymentNotificationRequest request) {
        var template = BODY_TEMPLATES.get(request.getEventType());
        if (template == null) {
            log.warn("No body template found for payment event type: {}", request.getEventType());
            return "Your payment " + request.getPaymentReference() + " has been updated.";
        }

        return String.format(template,
                request.getCustomerName(),
                request.getCurrency(),
                request.getAmount(),
                request.getPaymentReference(),
                request.getOrderNumber(),
                resolveFailureReason(request));
    }

    private String resolveFailureReason(PaymentNotificationRequest request) {
        if (request.getFailureReason() == null || request.getFailureReason().isBlank()) {
            return "the payment was declined";
        }
        return request.getFailureReason();
    }
}