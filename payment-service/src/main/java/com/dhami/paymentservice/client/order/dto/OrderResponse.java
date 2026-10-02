package com.dhami.paymentservice.client.order.dto;

public record OrderResponse(
        Long id,
        String orderNumber,
        String status
) {
}
