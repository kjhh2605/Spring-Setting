package com.example.auth.application.port.in.command;

import com.example.auth.application.port.in.command.dto.RefreshTokenCommand;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;

public interface SessionUseCase {
    TokenPairInfo issue(long userId);

    TokenPairInfo refresh(RefreshTokenCommand command);

    void logout(RefreshTokenCommand command);
}
