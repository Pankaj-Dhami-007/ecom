package com.dhami.ecom.product_service.dto;

import java.time.Instant;

public record Meta(
        Instant timestamp
) {
    public static Meta now() {
        return new Meta(Instant.now());
    }
}
