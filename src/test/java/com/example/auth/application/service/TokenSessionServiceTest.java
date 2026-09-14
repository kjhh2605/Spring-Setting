package com.example.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.auth.application.port.in.command.dto.TokenPairInfo;
import com.example.auth.application.port.out.AccessTokenIssuer;
import com.example.auth.application.port.out.RefreshSessionStore;
import com.example.auth.application.port.out.RefreshTokenGenerator;
import com.example.auth.domain.RefreshRotation;
import com.example.auth.domain.RefreshSession;
import com.example.auth.domain.TokenPolicy;
import com.example.shared.error.BusinessException;

@ExtendWith(MockitoExtension.class)
class TokenSessionServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-14T00:00:00Z");

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    @Mock
    private RefreshSessionStore refreshSessionStore;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    private TokenSessionService service;

    @BeforeEach
    void setUp() {
        service = new TokenSessionService(
                accessTokenIssuer,
                refreshSessionStore,
                refreshTokenGenerator,
                new TokenPolicy(Duration.ofMinutes(15), Duration.ofDays(14)),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void issuesAccessAndOpaqueRefreshTokens() {
        when(refreshTokenGenerator.generate()).thenReturn("refresh-token");
        when(accessTokenIssuer.issue(1L, "session-1", true)).thenReturn("access-token");

        TokenPairInfo result = service.issue(1L, true, "session-1", "family-1");

        assertThat(result).isEqualTo(new TokenPairInfo("access-token", "refresh-token", Duration.ofDays(14), true));
        verify(refreshSessionStore)
                .create(
                        "refresh-token",
                        new RefreshSession(1L, "session-1", "family-1", true, NOW.plus(Duration.ofDays(14))));
    }

    @Test
    void rotatesRefreshTokenAndIssuesNewAccessToken() {
        RefreshSession session = new RefreshSession(1L, "session-1", "family-1", false, NOW.plus(Duration.ofDays(14)));
        when(refreshTokenGenerator.generate()).thenReturn("next-refresh");
        when(refreshSessionStore.rotate("current-refresh", "next-refresh"))
                .thenReturn(RefreshRotation.rotated(session));
        when(accessTokenIssuer.issue(1L, "session-1", false)).thenReturn("next-access");

        TokenPairInfo result = service.refresh("current-refresh");

        assertThat(result).isEqualTo(new TokenPairInfo("next-access", "next-refresh", Duration.ofDays(14), false));
    }

    @Test
    void rejectsReusedRefreshToken() {
        when(refreshTokenGenerator.generate()).thenReturn("next-refresh");
        when(refreshSessionStore.rotate("used-refresh", "next-refresh")).thenReturn(RefreshRotation.reused());

        assertThatThrownBy(() -> service.refresh("used-refresh")).isInstanceOf(BusinessException.class);
    }
}
