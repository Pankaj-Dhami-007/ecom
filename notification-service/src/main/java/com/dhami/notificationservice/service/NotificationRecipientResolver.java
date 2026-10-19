package com.dhami.notificationservice.service;

import com.dhami.notificationservice.config.NotificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRecipientResolver {

    private final NotificationProperties properties;

    public String resolveEmail(Long customerId) {
        String email = properties.getRecipient().getEmail();

        if (email == null || email.isBlank()) {
            log.warn("No notifications.recipient.email configured - customer id: {} will not be notified", customerId);
            return email;
        }
        log.debug("Resolved notification address for customer id: {}", customerId);
        return email;
    }
    public String resolveDisplayName(Long customerId) {
        return "Customer " + customerId;
    }
}