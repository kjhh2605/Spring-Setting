package com.example.auth.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtAccessTokenIssuerTest {

    @Test
    void signsAccessTokenWithConfiguredClaims() {
        Instant now = Instant.parse("2099-09-14T00:00:00Z");
        SecretKey key = JwtSecurityConfig.secretKey("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        JwtAccessTokenIssuer issuer = new JwtAccessTokenIssuer(
                NimbusJwtEncoder.withSecretKey(key)
                        .algorithm(MacAlgorithm.HS256)
                        .build(),
                "https://api.orbit.test",
                Duration.ofMinutes(15),
                Clock.fixed(now, ZoneOffset.UTC));
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        Jwt jwt = decoder.decode(issuer.issue(7L, "session-1", true));

        assertThat(jwt.getIssuer().toString()).isEqualTo("https://api.orbit.test");
        assertThat(jwt.getSubject()).isEqualTo("user:7");
        assertThat(jwt.getClaimAsString("sid")).isEqualTo("session-1");
        assertThat(jwt.getClaimAsBoolean("onboarding_required")).isTrue();
        assertThat(jwt.getExpiresAt()).isEqualTo(now.plus(Duration.ofMinutes(15)));
    }
}
