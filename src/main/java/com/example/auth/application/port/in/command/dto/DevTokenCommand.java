package com.example.auth.application.port.in.command.dto;

public record DevTokenCommand(String secret, long userId) {
    @Override
    public String toString() {
        return "DevTokenCommand[redacted]";
    }
}
