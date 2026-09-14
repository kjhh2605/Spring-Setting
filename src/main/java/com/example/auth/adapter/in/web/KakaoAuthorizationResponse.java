package com.example.auth.adapter.in.web;

import com.example.auth.application.port.in.command.dto.KakaoAuthorizationInfo;

import io.swagger.v3.oas.annotations.media.Schema;

public record KakaoAuthorizationResponse(
        @Schema(description = "카카오 로그인 페이지 URL") String authorizationUrl) {

    static KakaoAuthorizationResponse from(KakaoAuthorizationInfo info) {
        return new KakaoAuthorizationResponse(info.authorizationUrl());
    }
}
