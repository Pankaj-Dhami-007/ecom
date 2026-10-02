package com.dhami.authservice.common.dto;

public record ApiError(
     int code,
     String message
) {
}
