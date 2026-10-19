package com.dhami.notificationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Everything the payment email needs, assembled by the listener from an event.
 *
 * <p>Mirrors how {@link OrderNotificationRequest} works, so the two notification paths stay
 * recognisable. Kept separate rather than merged because the fields differ: a payment notice
 * quotes a payment reference and a failure reason, an order notice does not.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNotificationRequest {

    @NotBlank(message = "Recipient email must not be blank")
    @Email(message = "Invalid recipient email format")
    private String to;

    @NotBlank(message = "Customer name must not be blank")
    private String customerName;

    @NotBlank(message = "Order number must not be blank")
    private String orderNumber;

    @NotBlank(message = "Payment reference must not be blank")
    private String paymentReference;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Currency must not be blank")
    private String currency;

    /**
     * Gateway supplied reason for the failure. Only set when {@code eventType} is
     * PAYMENT_FAILED, and ignored otherwise.
     */
    private String failureReason;

    @NotNull(message = "Event type is required")
    private PaymentEventType eventType;
}