package com.example.activity.adapter.in.event;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.example.activity.application.port.in.RecordUserRegistrationActivityUseCase;
import com.example.user.UserRegistered;

@Component
public class UserRegisteredListener {

    private final RecordUserRegistrationActivityUseCase useCase;

    public UserRegisteredListener(RecordUserRegistrationActivityUseCase useCase) {
        this.useCase = useCase;
    }

    @ApplicationModuleListener
    public void on(UserRegistered event) {
        useCase.record(event);
    }
}
