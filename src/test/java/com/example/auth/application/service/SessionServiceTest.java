package com.example.auth.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.in.command.dto.RefreshTokenCommand;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;
import com.example.auth.application.port.out.LoadAuthSubjectPort;
import com.example.auth.application.port.out.RefreshSessionPort;
import com.example.auth.application.port.out.TokenPort;
import com.example.auth.domain.AuthSubject;
import com.example.auth.domain.RefreshIdentity;
import com.example.shared.error.BusinessException;

class SessionServiceTest {
    private final TokenPort tokens = mock(TokenPort.class);
    private final RefreshSessionPort sessions = mock(RefreshSessionPort.class);
    private final LoadAuthSubjectPort subjects = mock(LoadAuthSubjectPort.class);
    private final SessionService service = new SessionService(tokens, sessions, subjects);

    @Test
    void invalidSignatureCannotRevokeOrRotateRedisSession() {
        when(tokens.verifyRefresh("invalid")).thenThrow(new BusinessException(AuthErrorCode.INVALID_TOKEN));
        assertThatThrownBy(() -> service.refresh(new RefreshTokenCommand("invalid")))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(sessions, subjects);
    }

    @Test
    void missingUserCannotObtainTokens() {
        when(subjects.findByUserId(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.issue(1)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(tokens, sessions);
    }

    @Test
    void missingUserRevokesExistingRefreshSession() {
        var identity = new RefreshIdentity(1, "sid", "jti", Instant.parse("2026-10-01T00:00:00Z"));
        when(tokens.verifyRefresh("refresh")).thenReturn(identity);
        when(subjects.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(new RefreshTokenCommand("refresh")))
                .isInstanceOf(BusinessException.class);
        verify(sessions).revoke(identity);
    }

    @Test
    void redisFailurePreventsTokenResponse() {
        var expiry = Instant.parse("2026-10-01T00:00:00Z");
        var identity = new RefreshIdentity(1, "sid", "jti", expiry);
        var pair = new TokenPairInfo("access", "refresh", expiry, expiry);
        when(subjects.findByUserId(1L)).thenReturn(Optional.of(new AuthSubject(1L)));
        when(tokens.issue(1)).thenReturn(pair);
        when(tokens.readIssuedRefresh(pair)).thenReturn(identity);
        org.mockito.Mockito.doThrow(new BusinessException(AuthErrorCode.SESSION_STORE_UNAVAILABLE))
                .when(sessions)
                .create(identity);

        assertThatThrownBy(() -> service.issue(1)).isInstanceOf(BusinessException.class);
    }
}
