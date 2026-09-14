package com.example.auth.application.port.in.command;

public interface LogoutUseCase {

    void logout(String refreshToken);
}
