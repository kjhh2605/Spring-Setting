package com.example.auth.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.media.Schema;

public record DevTokenRequest(
        @Schema(description = "기존 사용자 ID", example = "1") @NotNull @Positive
        Long userId) {}
