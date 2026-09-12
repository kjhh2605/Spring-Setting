package com.example.auth.application.service;

import org.springframework.stereotype.Service;

import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.in.command.SessionUseCase;
import com.example.auth.application.port.in.command.dto.RefreshTokenCommand;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;
import com.example.auth.application.port.out.LoadAuthSubjectPort;
import com.example.auth.application.port.out.RefreshSessionPort;
import com.example.auth.application.port.out.TokenPort;
import com.example.shared.error.BusinessException;

@Service
public class SessionService implements SessionUseCase {
    private final TokenPort tokens;
    private final RefreshSessionPort sessions;
    private final LoadAuthSubjectPort subjects;

    public SessionService(TokenPort tokens, RefreshSessionPort sessions, LoadAuthSubjectPort subjects) {
        this.tokens = tokens;
        this.sessions = sessions;
        this.subjects = subjects;
    }

    @Override
    public TokenPairInfo issue(long userId) {
        requireUser(userId);
        TokenPairInfo pair = tokens.issue(userId);
        sessions.create(tokens.readIssuedRefresh(pair));
        return pair;
    }

    @Override
    public TokenPairInfo refresh(RefreshTokenCommand command) {
        var previous = tokens.verifyRefresh(command.refreshToken());
        if (subjects.findByUserId(previous.userId()).isEmpty()) {
            sessions.revoke(previous);
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }
        TokenPairInfo pair = tokens.rotate(previous);
        if (!sessions.rotate(previous, tokens.readIssuedRefresh(pair))) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }
        return pair;
    }

    @Override
    public void logout(RefreshTokenCommand command) {
        sessions.revoke(tokens.verifyRefresh(command.refreshToken()));
    }

    private void requireUser(long userId) {
        subjects.findByUserId(userId).orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));
    }
}
