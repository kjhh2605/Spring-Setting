package com.example.user.application.port.in;

public interface RegisterUserUseCase {

    RegisteredUserInfo register(RegisterUserCommand command);
}
