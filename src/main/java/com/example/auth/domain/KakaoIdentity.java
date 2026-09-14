package com.example.auth.domain;

public record KakaoIdentity(String providerUserId, String displayName) {

    public KakaoIdentity {
        requireText(providerUserId, "providerUserId");
        requireText(displayName, "displayName");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
