package com.example.user.domain;

import java.util.Objects;
import java.util.Optional;

public final class User {

    private static final int MAX_DISPLAY_NAME_LENGTH = 100;

    private final UserId id;
    private final String displayName;
    private final UserStatus status;

    private User(UserId id, String displayName, UserStatus status) {
        this.id = id;
        this.displayName = validateDisplayName(displayName);
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public static User register(String displayName) {
        return new User(null, displayName, UserStatus.ACTIVE);
    }

    public static User registerPending(String displayName) {
        return new User(null, displayName, UserStatus.PENDING_ONBOARDING);
    }

    public static User reconstitute(UserId id, String displayName) {
        return reconstitute(id, displayName, UserStatus.ACTIVE);
    }

    public static User reconstitute(UserId id, String displayName, UserStatus status) {
        return new User(Objects.requireNonNull(id, "id must not be null"), displayName, status);
    }

    public Optional<UserId> id() {
        return Optional.ofNullable(id);
    }

    public String displayName() {
        return displayName;
    }

    public UserStatus status() {
        return status;
    }

    public boolean onboardingRequired() {
        return status == UserStatus.PENDING_ONBOARDING;
    }

    private static String validateDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        if (displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new IllegalArgumentException("displayName must be at most 100 characters");
        }
        return displayName;
    }
}
