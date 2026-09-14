package com.example.auth.application.port.in.command;

import com.example.auth.application.port.in.command.dto.KakaoAuthorizationInfo;

public interface CreateKakaoAuthorizationUseCase {

    KakaoAuthorizationInfo create();
}
