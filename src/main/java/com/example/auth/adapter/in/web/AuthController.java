package com.example.auth.adapter.in.web;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.adapter.in.web.docs.AuthControllerDocs;
import com.example.auth.application.port.in.command.LoginUseCase;
import com.example.auth.application.port.in.command.SessionUseCase;
import com.example.auth.application.port.in.command.dto.RefreshTokenCommand;
import com.example.auth.application.port.in.command.dto.SocialLoginCommand;

@RestController
@Validated
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs {
    private final LoginUseCase login;
    private final SessionUseCase sessions;

    public AuthController(LoginUseCase login, SessionUseCase sessions) {
        this.login = login;
        this.sessions = sessions;
    }

    @Override
    @PostMapping("/social/{provider}")
    public TokenResponse login(
            @PathVariable String provider, @RequestBody SocialLoginRequest request, HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return TokenResponse.from(login.login(new SocialLoginCommand(provider, request.accessToken())));
    }

    @Override
    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshTokenRequest request, HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return TokenResponse.from(sessions.refresh(new RefreshTokenCommand(request.refreshToken())));
    }

    @Override
    @PostMapping("/logout")
    public void logout(@RequestBody RefreshTokenRequest request) {
        sessions.logout(new RefreshTokenCommand(request.refreshToken()));
    }
}
