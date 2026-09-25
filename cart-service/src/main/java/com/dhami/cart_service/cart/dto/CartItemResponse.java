package com.dhami.cart_service.cart.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}