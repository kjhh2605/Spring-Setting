package com.example.auth.application.port.in.command.dto;

public record KakaoLoginCommand(String authorizationCode, String state) {

    public KakaoLoginCommand {
        requireText(authorizationCode, "authorizationCode");
        requireText(state, "state");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
