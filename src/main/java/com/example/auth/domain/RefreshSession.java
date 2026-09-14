package com.example.auth.domain;

import java.time.Instant;
import java.util.Objects;

public record RefreshSession(
        long userId, String sessionId, String familyId, boolean onboardingRequired, Instant expiresAt) {

    public RefreshSession {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
        requireText(sessionId, "sessionId");
        requireText(familyId, "familyId");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
