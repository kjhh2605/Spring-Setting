package com.example.auth.application.port.in.command.dto;

public record RefreshTokenCommand(String refreshToken) {
    @Override
    public String toString() {
        return "RefreshTokenCommand[redacted]";
    }
}
