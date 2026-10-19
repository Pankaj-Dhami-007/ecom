package com.dhami.notificationservice.dto;

/**
 * The payment outcomes this service can send an email about.
 *
 * <p>Separate from {@link OrderEventType} on purpose. An order email and a payment email are
 * different subjects with different wording, and keeping them in separate enums stops one
 * from growing entries the other can never use.
 */
public enum PaymentEventType {
    PAYMENT_SUCCEEDED,
    PAYMENT_FAILED
}