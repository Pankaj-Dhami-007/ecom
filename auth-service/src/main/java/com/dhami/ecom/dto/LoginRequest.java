package com.dhami.ecom.dto;

public record LoginRequest(
        String email,
        String password
) {
}
