package com.example.auth.domain;

public record OAuthLoginRequest(String nonce, String codeVerifier) {

    public OAuthLoginRequest {
        requireText(nonce, "nonce");
        requireText(codeVerifier, "codeVerifier");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
