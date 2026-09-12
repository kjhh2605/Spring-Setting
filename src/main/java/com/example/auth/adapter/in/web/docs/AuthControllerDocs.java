package com.example.auth.adapter.in.web.docs;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import com.example.auth.adapter.in.web.RefreshTokenRequest;
import com.example.auth.adapter.in.web.SocialLoginRequest;
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

@Tag(name = "Auth", description = "소셜 로그인과 토큰 회전")
public interface AuthControllerDocs {
    @Operation(summary = "소셜 로그인", description = "카카오 access token을 검증하고 가입 또는 로그인 후 서비스 토큰을 발급합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "발급 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class)))
    @ApiErrorCodes(enumClass = CommonErrorCode.class, includes = "BAD_REQUEST")
    @ApiErrorCodes(
            enumClass = AuthErrorCode.class,
            includes = {
                "UNSUPPORTED_PROVIDER",
                "INVALID_SOCIAL_TOKEN",
                "SOCIAL_PROVIDER_UNAVAILABLE",
                "SESSION_STORE_UNAVAILABLE"
            })
    TokenResponse login(
            @Parameter(description = "제공자 이름. 현재 kakao만 지원", example = "kakao") @NotBlank String provider,
            @Valid SocialLoginRequest request,
            @Parameter(hidden = true) HttpServletResponse response);

    @Operation(summary = "토큰 재발급", description = "refresh token을 일회 사용하여 회전합니다. 재사용이 감지되면 해당 세션을 폐기합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "회전 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class)))
    @ApiErrorCodes(enumClass = CommonErrorCode.class, includes = "BAD_REQUEST")
    @ApiErrorCodes(
            enumClass = AuthErrorCode.class,
            includes = {"INVALID_TOKEN", "SESSION_STORE_UNAVAILABLE"})
    TokenResponse refresh(@Valid RefreshTokenRequest request, @Parameter(hidden = true) HttpServletResponse response);

    @Operation(summary = "로그아웃", description = "해당 refresh 세션을 폐기합니다. 발급된 access token은 만료시각까지 유효합니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @ApiErrorCodes(enumClass = CommonErrorCode.class, includes = "BAD_REQUEST")
    @ApiErrorCodes(
            enumClass = AuthErrorCode.class,
            includes = {"INVALID_TOKEN", "SESSION_STORE_UNAVAILABLE"})
    void logout(@Valid RefreshTokenRequest request);
}
