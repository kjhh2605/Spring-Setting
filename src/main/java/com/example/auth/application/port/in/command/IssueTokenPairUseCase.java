package com.example.auth.application.port.in.command;

import com.example.auth.application.port.in.command.dto.TokenPairInfo;

public interface IssueTokenPairUseCase {

    TokenPairInfo issue(long userId, boolean onboardingRequired);
}
