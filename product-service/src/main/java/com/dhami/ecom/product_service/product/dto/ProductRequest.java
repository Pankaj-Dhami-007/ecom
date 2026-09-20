package com.dhami.ecom.product_service.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be zero or positive")
        BigDecimal price,

        @PositiveOrZero(message = "Compare price must be zero or positive")
        BigDecimal comparePrice,

        @NotBlank(message = "SKU is required")
        String sku,

        @PositiveOrZero(message = "Stock quantity must be zero or positive")
        Integer stockQuantity,

        String imageUrl,

        Boolean active,

        @NotNull(message = "Category ID is required")
        Long categoryId
) {
}
