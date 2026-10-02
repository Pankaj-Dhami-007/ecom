package com.dhami.authservice.common.dto;

import java.time.Instant;

public record Meta(
        Instant timestamp
) {
    public static Meta now(){
        return new Meta(Instant.now());
    }
}
