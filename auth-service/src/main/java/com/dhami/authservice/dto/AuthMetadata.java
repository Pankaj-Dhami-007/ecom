package com.dhami.authservice.dto;

public record AuthMetadata(
        String email,
        long accessExpiresInSeconds
) {}
