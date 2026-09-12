package com.example.auth.adapter.in.web;

import java.time.Instant;

import com.example.auth.application.port.in.command.dto.TokenPairInfo;

public record TokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt) {
    public static TokenResponse from(TokenPairInfo info) {
        return new TokenResponse(
                "Bearer",
                info.accessToken(),
                info.refreshToken(),
                info.accessTokenExpiresAt(),
                info.refreshTokenExpiresAt());
    }

    @Override
    public String toString() {
        return "TokenResponse[redacted]";
    }
}
