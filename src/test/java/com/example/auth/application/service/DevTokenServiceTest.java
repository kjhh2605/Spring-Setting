package com.example.auth.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.example.auth.application.port.in.command.SessionUseCase;
import com.example.auth.application.port.in.command.dto.DevTokenCommand;
import com.example.auth.application.port.out.DevCredentialPort;
import com.example.shared.error.BusinessException;

class DevTokenServiceTest {
    @Test
    void requiresSecretBeforeLookingUpOrIssuingForUser() {
        var credentials = mock(DevCredentialPort.class);
        var sessions = mock(SessionUseCase.class);
        var service = new DevTokenService(credentials, sessions);

        assertThatThrownBy(() -> service.issue(new DevTokenCommand("wrong", 1))).isInstanceOf(BusinessException.class);
        verifyNoInteractions(sessions);
        when(credentials.matches("valid")).thenReturn(true);
        service.issue(new DevTokenCommand("valid", 1));
        verify(sessions).issue(1);
    }
}
