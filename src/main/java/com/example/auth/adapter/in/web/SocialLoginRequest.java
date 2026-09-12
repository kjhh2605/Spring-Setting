package com.example.auth.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public record SocialLoginRequest(
        @Schema(description = "프론트 SDK에서 발급받은 카카오 access token") @NotBlank @Size(max = 4096)
        String accessToken) {
    @Override
    public String toString() {
        return "SocialLoginRequest[redacted]";
    }
}
