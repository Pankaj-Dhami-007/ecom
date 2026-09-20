package com.dhami.ecom.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long accessExpiresIn,   // seconds
        long refreshExpiresIn,
        String message
) {
}
