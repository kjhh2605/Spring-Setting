package com.example.auth.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

import com.example.auth.application.port.in.command.dto.KakaoLoginCommand;

import io.swagger.v3.oas.annotations.media.Schema;

public record KakaoLoginRequest(
        @NotBlank @Schema(description = "카카오 인가 코드", example = "authorization-code")
        String authorizationCode,

        @NotBlank @Schema(description = "인가 요청과 연결된 일회용 상태값", example = "random-state")
        String state) {

    KakaoLoginCommand toCommand() {
        return new KakaoLoginCommand(authorizationCode, state);
    }
}
