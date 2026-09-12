package com.example.auth.adapter.out.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

import com.example.shared.error.BusinessException;

class JwtTokenAdapterTest {
    private final Instant now = Instant.parse("2026-09-12T00:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    private final TokenProperties properties = new TokenProperties(
            Base64.getEncoder().encodeToString(new byte[32]),
            "test",
            "api",
            "refresh",
            Duration.ofMinutes(15),
            Duration.ofDays(14),
            Duration.ZERO);
    private final AuthTokenConfiguration configuration = new AuthTokenConfiguration();

    @Test
    void distinguishesAccessAndRefreshAndRotatesWithoutExtendingSession() {
        JwtTokenAdapter adapter = adapter(clock);
        var pair = adapter.issue(7);
        var previous = adapter.verifyRefresh(pair.refreshToken());
        var next = adapter(Clock.fixed(now.plusSeconds(60), ZoneOffset.UTC)).rotate(previous);

        assertThat(configuration
                        .accessJwtDecoder(properties, clock)
                        .decode(pair.accessToken())
                        .getSubject())
                .isEqualTo("user:7");
        assertThatThrownBy(() -> adapter.verifyRefresh(pair.accessToken())).isInstanceOf(BusinessException.class);
        assertThatThrownBy(
                        () -> configuration.accessJwtDecoder(properties, clock).decode(pair.refreshToken()))
                .isInstanceOf(JwtException.class);
        assertThat(next.refreshToken()).isNotEqualTo(pair.refreshToken());
        assertThat(next.refreshTokenExpiresAt()).isEqualTo(pair.refreshTokenExpiresAt());
        assertThat(adapter.verifyRefresh(next.refreshToken()).sessionId()).isEqualTo(previous.sessionId());
    }

    @Test
    void rejectsExpiredAndTamperedRefreshTokens() {
        var pair = adapter(clock).issue(7);
        assertThatThrownBy(() -> adapter(Clock.fixed(now.plus(Duration.ofDays(14)), ZoneOffset.UTC))
                        .verifyRefresh(pair.refreshToken()))
                .isInstanceOf(BusinessException.class);
        String[] parts = pair.refreshToken().split("\\.");
        String tampered =
                parts[0] + "." + parts[1] + "." + (parts[2].startsWith("A") ? "B" : "A") + parts[2].substring(1);
        assertThatThrownBy(() -> adapter(clock).verifyRefresh(tampered)).isInstanceOf(BusinessException.class);
    }

    private JwtTokenAdapter adapter(Clock useClock) {
        return new JwtTokenAdapter(
                configuration.jwtEncoder(properties),
                configuration.refreshJwtDecoder(properties, useClock),
                properties,
                useClock);
    }
}
