package com.example.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.example.user.UserRegistered;
import com.example.user.application.port.out.SocialUserRepository;
import com.example.user.application.port.out.SocialUserResult;
import com.example.user.domain.User;
import com.example.user.domain.UserId;

class SocialUserServiceTest {
    @Test
    void publishesRegistrationOnlyForNewSocialUser() {
        var repository = mock(SocialUserRepository.class);
        var events = mock(ApplicationEventPublisher.class);
        Instant now = Instant.parse("2026-09-12T00:00:00Z");
        var service = new SocialUserService(repository, events, Clock.fixed(now, ZoneOffset.UTC));
        var user = User.reconstitute(new UserId(1L), "카카오 사용자");
        when(repository.findOrCreate(any())).thenReturn(new SocialUserResult(user, false));

        assertThat(service.findOrRegister("kakao", "123", "카카오 사용자").id()).isEqualTo(1);
        verifyNoInteractions(events);

        when(repository.findOrCreate(any())).thenReturn(new SocialUserResult(user, true));
        service.findOrRegister("kakao", "123", "카카오 사용자");
        verify(events).publishEvent(new UserRegistered(1L, now));
    }
}
