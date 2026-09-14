package com.example.auth.application.service;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.in.command.IssueTokenPairUseCase;
import com.example.auth.application.port.in.command.LogoutAllUseCase;
import com.example.auth.application.port.in.command.LogoutUseCase;
import com.example.auth.application.port.in.command.RefreshTokensUseCase;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;
import com.example.auth.application.port.out.AccessTokenIssuer;
import com.example.auth.application.port.out.RefreshSessionStore;
import com.example.auth.application.port.out.RefreshTokenGenerator;
import com.example.auth.domain.RefreshRotation;
import com.example.auth.domain.RefreshSession;
import com.example.auth.domain.TokenPolicy;
import com.example.shared.error.BusinessException;

@Service
public class TokenSessionService
        implements IssueTokenPairUseCase, RefreshTokensUseCase, LogoutUseCase, LogoutAllUseCase {

    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshSessionStore refreshSessionStore;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final TokenPolicy tokenPolicy;
    private final Clock clock;

    public TokenSessionService(
            AccessTokenIssuer accessTokenIssuer,
            RefreshSessionStore refreshSessionStore,
            RefreshTokenGenerator refreshTokenGenerator,
            TokenPolicy tokenPolicy,
            Clock clock) {
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshSessionStore = refreshSessionStore;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.tokenPolicy = tokenPolicy;
        this.clock = clock;
    }

    @Override
    public TokenPairInfo issue(long userId, boolean onboardingRequired) {
        return issue(
                userId,
                onboardingRequired,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
    }

    TokenPairInfo issue(long userId, boolean onboardingRequired, String sessionId, String familyId) {
        String refreshToken = refreshTokenGenerator.generate();
        RefreshSession session = new RefreshSession(
                userId, sessionId, familyId, onboardingRequired, clock.instant().plus(tokenPolicy.refreshTokenTtl()));
        refreshSessionStore.create(refreshToken, session);
        return tokenPair(refreshToken, session, tokenPolicy.refreshTokenTtl());
    }

    @Override
    public TokenPairInfo refresh(String refreshToken) {
        String nextRefreshToken = refreshTokenGenerator.generate();
        RefreshRotation rotation = refreshSessionStore.rotate(refreshToken, nextRefreshToken);
        if (rotation.status() == RefreshRotation.Status.REUSED) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_REUSED);
        }
        RefreshSession session =
                rotation.rotatedSession().orElseThrow(() -> new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID));
        Duration remainingTtl = Duration.between(clock.instant(), session.expiresAt());
        if (remainingTtl.isZero() || remainingTtl.isNegative()) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }
        return tokenPair(nextRefreshToken, session, remainingTtl);
    }

    @Override
    public void logout(String refreshToken) {
        refreshSessionStore.revoke(refreshToken);
    }

    @Override
    public void logoutAll(long userId) {
        refreshSessionStore.revokeAll(userId);
    }

    private TokenPairInfo tokenPair(String refreshToken, RefreshSession session, Duration refreshTokenTtl) {
        String accessToken =
                accessTokenIssuer.issue(session.userId(), session.sessionId(), session.onboardingRequired());
        return new TokenPairInfo(accessToken, refreshToken, refreshTokenTtl, session.onboardingRequired());
    }
}
