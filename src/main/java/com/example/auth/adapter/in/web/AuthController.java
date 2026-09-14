package com.example.auth.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.adapter.in.web.docs.AuthControllerDocs;
import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.in.command.CreateKakaoAuthorizationUseCase;
import com.example.auth.application.port.in.command.LoginWithKakaoUseCase;
import com.example.auth.application.port.in.command.LogoutAllUseCase;
import com.example.auth.application.port.in.command.LogoutUseCase;
import com.example.auth.application.port.in.command.RefreshTokensUseCase;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;
import com.example.shared.error.BusinessException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs {

    private final CreateKakaoAuthorizationUseCase createAuthorizationUseCase;
    private final LoginWithKakaoUseCase loginWithKakaoUseCase;
    private final RefreshTokensUseCase refreshTokensUseCase;
    private final LogoutUseCase logoutUseCase;
    private final LogoutAllUseCase logoutAllUseCase;
    private final RefreshTokenCookieManager cookieManager;

    public AuthController(
            CreateKakaoAuthorizationUseCase createAuthorizationUseCase,
            LoginWithKakaoUseCase loginWithKakaoUseCase,
            RefreshTokensUseCase refreshTokensUseCase,
            LogoutUseCase logoutUseCase,
            LogoutAllUseCase logoutAllUseCase,
            RefreshTokenCookieManager cookieManager) {
        this.createAuthorizationUseCase = createAuthorizationUseCase;
        this.loginWithKakaoUseCase = loginWithKakaoUseCase;
        this.refreshTokensUseCase = refreshTokensUseCase;
        this.logoutUseCase = logoutUseCase;
        this.logoutAllUseCase = logoutAllUseCase;
        this.cookieManager = cookieManager;
    }

    @Override
    @PostMapping("/kakao/authorization-requests")
    public KakaoAuthorizationResponse createKakaoAuthorization() {
        return KakaoAuthorizationResponse.from(createAuthorizationUseCase.create());
    }

    @Override
    @PostMapping("/kakao/login")
    public AuthTokenResponse login(@Valid @RequestBody KakaoLoginRequest request, HttpServletResponse response) {
        return respondWithTokens(loginWithKakaoUseCase.login(request.toCommand()), response);
    }

    @Override
    @PostMapping("/tokens/refresh")
    public AuthTokenResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = requiredRefreshToken(request);
        return respondWithTokens(refreshTokensUseCase.refresh(refreshToken), response);
    }

    @Override
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        cookieManager.read(request).ifPresent(logoutUseCase::logout);
        cookieManager.clear(response);
    }

    @Override
    @PostMapping("/logout-all")
    public void logoutAll(@AuthenticationPrincipal Jwt jwt, HttpServletResponse response) {
        Number userId = jwt.getClaim("uid");
        logoutAllUseCase.logoutAll(userId.longValue());
        cookieManager.clear(response);
    }

    private AuthTokenResponse respondWithTokens(TokenPairInfo tokens, HttpServletResponse response) {
        cookieManager.write(response, tokens.refreshToken(), tokens.refreshTokenTtl());
        return AuthTokenResponse.from(tokens);
    }

    private String requiredRefreshToken(HttpServletRequest request) {
        return cookieManager
                .read(request)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID));
    }
}
