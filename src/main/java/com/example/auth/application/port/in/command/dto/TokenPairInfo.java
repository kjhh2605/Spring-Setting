package com.example.auth.application.port.in.command.dto;

import java.time.Instant;

public record TokenPairInfo(
        String accessToken, String refreshToken, Instant accessTokenExpiresAt, Instant refreshTokenExpiresAt) {
    @Override
    public String toString() {
        return "TokenPairInfo[redacted]";
    }
}
