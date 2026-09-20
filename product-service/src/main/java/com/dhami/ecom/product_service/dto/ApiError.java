package com.dhami.ecom.product_service.dto;

public record ApiError(
        int code,
        String message
) {
}
