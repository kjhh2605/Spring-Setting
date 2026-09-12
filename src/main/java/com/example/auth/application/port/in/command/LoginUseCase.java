package com.example.auth.application.port.in.command;

import com.example.auth.application.port.in.command.dto.SocialLoginCommand;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;

public interface LoginUseCase {
    TokenPairInfo login(SocialLoginCommand command);
}
