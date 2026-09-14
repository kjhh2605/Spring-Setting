package com.example.user.domain;

import java.util.Objects;

public record SocialAccount(UserId userId, SocialProvider provider, String providerUserId) {

    public SocialAccount {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("providerUserId must not be blank");
        }
    }

    public static SocialAccount link(UserId userId, SocialProvider provider, String providerUserId) {
        return new SocialAccount(userId, provider, providerUserId);
    }
}
