package com.dhami.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        long accessTtlSeconds,
        long refreshTtlSeconds
) {
}
