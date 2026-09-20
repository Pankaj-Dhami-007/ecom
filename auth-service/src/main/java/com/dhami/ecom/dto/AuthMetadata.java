package com.dhami.ecom.dto;

public record AuthMetadata(
        String email,
        long accessExpiresInSeconds
) {}
