package com.example.auth.application.port.in.command;

import com.example.auth.application.port.in.command.dto.KakaoLoginCommand;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;

public interface LoginWithKakaoUseCase {

    TokenPairInfo login(KakaoLoginCommand command);
}
