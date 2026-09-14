package com.example.auth.adapter.out.security;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.auth.jwt")
public record AuthTokenProperties(
        @NotBlank String issuer,
        @NotNull Duration accessTokenTtl,
        @NotNull Duration refreshTokenTtl,
        @NotNull Duration clockSkew,
        @NotBlank String secret) {

    public AuthTokenProperties {
        if (clockSkew != null && clockSkew.isNegative()) {
            throw new IllegalArgumentException("clockSkew must not be negative");
        }
    }
}
