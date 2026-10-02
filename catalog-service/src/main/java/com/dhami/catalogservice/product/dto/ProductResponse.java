package com.dhami.catalogservice.product.dto;

import com.dhami.catalogservice.category.dto.CategoryResponse;

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
        Integer reservedQuantity,
        String imageUrl,
        Boolean active,
        CategoryResponse category,
        Instant createdAt,
        Instant updatedAt
) {
}
