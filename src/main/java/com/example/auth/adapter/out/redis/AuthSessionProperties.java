package com.example.auth.adapter.out.redis;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.auth.session")
public record AuthSessionProperties(@Min(1) @Max(100) int maxPerUser) {}
