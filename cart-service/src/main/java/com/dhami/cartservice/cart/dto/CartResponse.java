package com.dhami.cartservice.cart.dto;

import com.dhami.cartservice.cart.enums.CartStatusEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CartResponse(
        Long id,
        Long userId,
        CartStatusEnum status,
        String currency,
        Integer itemCount,
        BigDecimal totalAmount,
        Long version,
        Instant createdAt,
        Instant updatedAt,
        List<CartItemResponse> items
) {
}