package com.dhami.notificationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationRequest {

    @NotBlank(message = "Recipient email must not be blank")
    @Email(message = "Invalid recipient email format")
    private String to;

    @NotBlank(message = "Customer name must not be blank")
    private String customerName;

    @NotBlank(message = "Order number must not be blank")
    private String orderNumber;

    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    @NotNull(message = "Event type is required")
    private OrderEventType eventType;
}
