package com.example.activity.application.port.in;

import com.example.user.UserRegistered;

public interface RecordUserRegistrationActivityUseCase {

    void record(UserRegistered event);
}
