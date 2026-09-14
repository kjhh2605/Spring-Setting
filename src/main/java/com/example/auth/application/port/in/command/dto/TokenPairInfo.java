package com.example.auth.application.port.in.command.dto;

import java.time.Duration;

public record TokenPairInfo(
        String accessToken, String refreshToken, Duration refreshTokenTtl, boolean onboardingRequired) {}
