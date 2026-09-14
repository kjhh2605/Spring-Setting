package com.example.auth.adapter.in.web;

import com.example.auth.application.port.in.command.dto.TokenPairInfo;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthTokenResponse(
        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "토큰 유형", example = "Bearer") String tokenType,

        @Schema(description = "추가 가입 절차 필요 여부", example = "true")
        boolean onboardingRequired) {

    static AuthTokenResponse from(TokenPairInfo tokens) {
        return new AuthTokenResponse(tokens.accessToken(), "Bearer", tokens.onboardingRequired());
    }
}
