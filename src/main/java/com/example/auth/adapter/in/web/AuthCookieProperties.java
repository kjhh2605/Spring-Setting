package com.example.auth.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.auth.cookie")
public record AuthCookieProperties(
        @NotBlank String refreshTokenName,
        @NotBlank String path,
        boolean secure,
        @Pattern(regexp = "Strict|Lax|None") String sameSite) {}
