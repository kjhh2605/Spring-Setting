package com.example.auth.adapter.out.token;

import java.time.Duration;
import java.util.Base64;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.auth.token")
public record TokenProperties(
        String secret,
        String issuer,
        String accessAudience,
        String refreshAudience,
        Duration accessTtl,
        Duration refreshTtl,
        Duration clockSkew) {
    public TokenProperties {
        if (secret == null
                || Base64.getDecoder().decode(secret).length < 32
                || issuer == null
                || issuer.isBlank()
                || accessAudience == null
                || accessAudience.isBlank()
                || refreshAudience == null
                || refreshAudience.isBlank()
                || accessAudience.equals(refreshAudience)
                || accessTtl == null
                || accessTtl.isNegative()
                || accessTtl.toSeconds() < 1
                || refreshTtl == null
                || refreshTtl.compareTo(accessTtl) <= 0
                || clockSkew == null
                || clockSkew.isNegative()) {
            throw new IllegalArgumentException("invalid app.auth.token configuration");
        }
    }

    @Override
    public String toString() {
        return "TokenProperties[redacted]";
    }
}
