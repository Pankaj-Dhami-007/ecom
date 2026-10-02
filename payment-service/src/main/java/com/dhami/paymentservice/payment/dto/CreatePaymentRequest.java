package com.dhami.paymentservice.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Sent by order-service the moment an order is created and its stock is reserved.
 *
 * customerId and cartId travel with the payment so payment-service can push the result
 * back to cart-service later without having to ask anyone for them.
 */
public record CreatePaymentRequest(

        @NotNull(message = "Order id is required")
        Long orderId,

        @NotNull(message = "Customer id is required")
        Long customerId,

        Long cartId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        String currency
) {
}
