package com.example.auth.adapter.in.web.docs;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.example.auth.adapter.in.web.DevTokenRequest;
import com.example.auth.adapter.in.web.TokenResponse;
import com.example.auth.application.error.AuthErrorCode;
import com.example.shared.error.CommonErrorCode;
import com.example.shared.openapi.ApiErrorCodes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Dev auth", description = "dev 전용 토큰 발급")
public interface DevTokenControllerDocs {
    @Operation(summary = "개발용 토큰 발급", description = "개발용 비밀키와 기존 사용자 ID로 일반 토큰을 발급합니다. prod에서는 제공하지 않습니다.")
    @ApiResponse(
            responseCode = "200",
            description = "발급 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class)))
    @ApiErrorCodes(enumClass = CommonErrorCode.class, includes = "BAD_REQUEST")
    @ApiErrorCodes(
            enumClass = AuthErrorCode.class,
            includes = {"USER_NOT_FOUND", "INVALID_DEV_SECRET", "SESSION_STORE_UNAVAILABLE"})
    TokenResponse issue(
            @Parameter(description = "개발용 비밀키") @NotBlank @Size(max = 4096) String secret,
            @Valid DevTokenRequest request,
            @Parameter(hidden = true) HttpServletResponse response);
}
