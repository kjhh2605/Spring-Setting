package com.example.auth.adapter.in.web.docs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.jwt.Jwt;

import com.example.auth.adapter.in.web.AuthTokenResponse;
import com.example.auth.adapter.in.web.KakaoAuthorizationResponse;
import com.example.auth.adapter.in.web.KakaoLoginRequest;
import com.example.auth.application.error.AuthErrorCode;
import com.example.shared.error.CommonErrorCode;
import com.example.shared.openapi.ApiErrorCodes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "카카오 로그인과 서비스 토큰 관리")
public interface AuthControllerDocs {

    @Operation(summary = "카카오 인가 요청 생성", description = "OIDC, state, nonce, PKCE가 적용된 카카오 로그인 URL을 생성합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "인가 URL 생성 성공",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KakaoAuthorizationResponse.class)))
    KakaoAuthorizationResponse createKakaoAuthorization();

    @Operation(summary = "카카오 로그인", description = "인가 코드를 검증하고 Access Token과 Refresh Token 쿠키를 발급합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthTokenResponse.class)))
    @ApiErrorCodes(enumClass = CommonErrorCode.class, includes = "BAD_REQUEST")
    @ApiErrorCodes(
            enumClass = AuthErrorCode.class,
            includes = {"KAKAO_LOGIN_FAILED", "OAUTH_REQUEST_INVALID"})
    AuthTokenResponse login(KakaoLoginRequest request, @Parameter(hidden = true) HttpServletResponse response);

    @Operation(summary = "토큰 갱신", description = "HttpOnly Refresh Token 쿠키를 회전하고 새 Access Token을 발급합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "갱신 성공",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthTokenResponse.class)))
    @ApiErrorCodes(
            enumClass = AuthErrorCode.class,
            includes = {"REFRESH_TOKEN_INVALID", "REFRESH_TOKEN_REUSED"})
    AuthTokenResponse refresh(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response);

    @Operation(summary = "현재 기기 로그아웃", description = "현재 Refresh Token family를 폐기하고 쿠키를 제거합니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    void logout(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response);

    @Operation(summary = "전체 기기 로그아웃", description = "인증 사용자의 모든 Refresh Token family를 폐기합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponse(responseCode = "200", description = "전체 로그아웃 성공")
    @ApiErrorCodes(enumClass = CommonErrorCode.class, includes = "UNAUTHORIZED")
    void logoutAll(@Parameter(hidden = true) Jwt jwt, @Parameter(hidden = true) HttpServletResponse response);
}
