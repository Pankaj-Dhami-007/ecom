package com.dhami.authservice.dto;

public record LoginRequest(
        String email,
        String password
) {
}
