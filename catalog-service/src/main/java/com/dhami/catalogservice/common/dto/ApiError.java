package com.dhami.catalogservice.common.dto;

public record ApiError(
        int code,
        String message
) {
}
