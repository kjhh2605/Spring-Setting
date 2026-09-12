package com.example.auth.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.in.command.DevTokenUseCase;
import com.example.auth.application.port.in.command.SessionUseCase;
import com.example.auth.application.port.in.command.dto.DevTokenCommand;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;
import com.example.auth.application.port.out.DevCredentialPort;
import com.example.shared.error.BusinessException;

@Service
@Profile("dev & !prod")
public class DevTokenService implements DevTokenUseCase {
    private final DevCredentialPort credentials;
    private final SessionUseCase sessions;

    public DevTokenService(DevCredentialPort credentials, SessionUseCase sessions) {
        this.credentials = credentials;
        this.sessions = sessions;
    }

    @Override
    public TokenPairInfo issue(DevTokenCommand command) {
        if (!credentials.matches(command.secret())) {
            throw new BusinessException(AuthErrorCode.INVALID_DEV_SECRET);
        }
        return sessions.issue(command.userId());
    }
}
