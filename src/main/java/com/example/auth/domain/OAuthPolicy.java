package com.example.auth.domain;

import java.time.Duration;
import java.util.Objects;

public record OAuthPolicy(Duration stateTtl) {

    public OAuthPolicy {
        Objects.requireNonNull(stateTtl, "stateTtl must not be null");
        if (stateTtl.isZero() || stateTtl.isNegative()) {
            throw new IllegalArgumentException("stateTtl must be positive");
        }
    }
}
