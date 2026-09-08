package com.example.activity.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

public record ActivityDescriptionResponse(
        @Schema(description = "활동 설명", example = "Activity for 홍길동")
        String description) {}
