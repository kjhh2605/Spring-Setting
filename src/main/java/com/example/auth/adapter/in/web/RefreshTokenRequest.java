package com.example.auth.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshTokenRequest(
        @Schema(description = "서비스가 발급한 refresh token") @NotBlank @Size(max = 4096)
        String refreshToken) {
    @Override
    public String toString() {
        return "RefreshTokenRequest[redacted]";
    }
}
