package com.example.auth.application.port.in.command.dto;

public record SocialLoginCommand(String provider, String accessToken) {
    @Override
    public String toString() {
        return "SocialLoginCommand[redacted]";
    }
}
