package com.example.auth.domain;

import java.util.Optional;

public record RefreshRotation(Status status, RefreshSession session) {

    public enum Status {
        ROTATED,
        INVALID,
        REUSED
    }

    public static RefreshRotation rotated(RefreshSession session) {
        return new RefreshRotation(Status.ROTATED, session);
    }

    public static RefreshRotation invalid() {
        return new RefreshRotation(Status.INVALID, null);
    }

    public static RefreshRotation reused() {
        return new RefreshRotation(Status.REUSED, null);
    }

    public Optional<RefreshSession> rotatedSession() {
        return Optional.ofNullable(session);
    }
}
