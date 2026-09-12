package com.example.auth.application.port.in.command;

import com.example.auth.application.port.in.command.dto.DevTokenCommand;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;

public interface DevTokenUseCase {
    TokenPairInfo issue(DevTokenCommand command);
}
