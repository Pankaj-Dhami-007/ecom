package com.dhami.ecom.product_service.common.dto;

public record ApiError(
        int code,
        String message
) {
}
