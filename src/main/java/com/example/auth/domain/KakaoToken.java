package com.example.auth.domain;

public record KakaoToken(String idToken) {

    public KakaoToken {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("idToken must not be blank");
        }
    }
}
