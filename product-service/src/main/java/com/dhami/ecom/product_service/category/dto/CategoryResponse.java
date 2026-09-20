package com.dhami.ecom.product_service.category.dto;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
