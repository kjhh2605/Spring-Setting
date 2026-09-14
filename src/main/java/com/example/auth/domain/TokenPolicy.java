package com.example.auth.domain;

import java.time.Duration;
import java.util.Objects;

public record TokenPolicy(Duration accessTokenTtl, Duration refreshTokenTtl) {

    public TokenPolicy {
        requirePositive(accessTokenTtl, "accessTokenTtl");
        requirePositive(refreshTokenTtl, "refreshTokenTtl");
    }

    private static void requirePositive(Duration duration, String name) {
        Objects.requireNonNull(duration, name + " must not be null");
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
