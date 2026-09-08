package com.example.activity.adapter.in.event;

import static org.mockito.Mockito.verify;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.activity.application.port.in.RecordUserRegistrationActivityUseCase;
import com.example.user.UserRegistered;

@ExtendWith(MockitoExtension.class)
class UserRegisteredListenerTest {

    @Mock
    private RecordUserRegistrationActivityUseCase useCase;

    @Test
    void delegatesRegisteredEvent() {
        UserRegistered event = new UserRegistered(1L, Instant.parse("2026-09-04T00:00:00Z"));

        new UserRegisteredListener(useCase).on(event);

        verify(useCase).record(event);
    }
}
