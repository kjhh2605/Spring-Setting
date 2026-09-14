package com.example.auth.domain;

public record LoginUser(long userId, boolean onboardingRequired) {

    public LoginUser {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }
}
