package com.dhami.notificationservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Notification settings that are not part of Spring's own configuration.
 *
 * <p>Bound from the {@code notifications.*} block in application.yaml.
 *
 * <p>These values live here rather than in each event handler because "who receives customer
 * email" is a single policy decision, not something every template should repeat.
 */
@Component
@ConfigurationProperties(prefix = "notifications")
@Getter
@Setter
public class NotificationProperties {

    /**
     * Where customer facing notifications are delivered.
     */
    private Recipient recipient = new Recipient();

    /**
     * Recipient settings for customer facing email.
     */
    @Getter
    @Setter
    public static class Recipient {

        /**
         * Address that customer notifications are sent to.
         *
         * <p>This exists because order-service only publishes a customer id, never an email
         * address, so notification-service has to decide on the recipient itself. The
         * obvious next step is to look the address up from auth-service by customer id;
         * until then a configured address is used and the customer id is logged alongside it,
         * so it is always obvious which customer a given notification was meant for.
         */
        private String email;
    }
}