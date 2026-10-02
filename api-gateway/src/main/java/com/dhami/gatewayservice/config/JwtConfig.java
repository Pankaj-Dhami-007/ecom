package com.dhami.gatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String secret;

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {

        byte[] keyBytes = Base64.getDecoder().decode(secret);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

        return NimbusReactiveJwtDecoder
                .withSecretKey(key)
                .build();
    }
}

// ReactiveJwtDecoder is the component used by Spring Security
// to validate incoming JWT tokens.
//
// When a request comes with:
// Authorization: Bearer <token>
//
// Spring Security calls this decoder to:
// 1. Verify the token signature
// 2. Check if the token is valid
// 3. Extract JWT claims (user info, roles, etc.)
//
// If validation fails -> 401 Unauthorized
// If validation succeeds -> request continues

// ReactiveJwtDecoder is used by Spring Security in reactive applications to
// validate JWT signatures and read claims from incoming tokens
