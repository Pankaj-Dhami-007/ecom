package com.dhami.ecom.product_service.product.dto;

import com.dhami.ecom.product_service.category.dto.CategoryResponse;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        BigDecimal comparePrice,
        String sku,
        Integer stockQuantity,
        String imageUrl,
        Boolean active,
        CategoryResponse category,
        Instant createdAt,
        Instant updatedAt
) {
}
