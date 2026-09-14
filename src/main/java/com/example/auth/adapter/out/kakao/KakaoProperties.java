package com.example.auth.adapter.out.kakao;

import java.net.URI;
import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.auth.kakao")
public record KakaoProperties(
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotNull URI redirectUri,
        @NotNull URI authorizationUri,
        @NotNull URI tokenUri,
        @NotNull URI issuer,
        @NotNull URI jwkSetUri,
        @NotNull Duration stateTtl) {}
