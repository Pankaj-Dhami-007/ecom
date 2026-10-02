package com.dhami.orderservice.client.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String to;
    private String customerName;
    private String orderNumber;
    private BigDecimal totalAmount;
    private String eventType;
}
