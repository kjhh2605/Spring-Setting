package com.example.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.example.user.ProvisionedUser;
import com.example.user.application.port.out.SocialAccountRepository;
import com.example.user.application.port.out.UserRepository;
import com.example.user.domain.SocialAccount;
import com.example.user.domain.SocialProvider;
import com.example.user.domain.User;
import com.example.user.domain.UserId;
import com.example.user.domain.UserStatus;

@ExtendWith(MockitoExtension.class)
class SocialAccountProvisioningServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-14T00:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SocialAccountProvisioningService service;

    @BeforeEach
    void setUp() {
        service = new SocialAccountProvisioningService(
                userRepository, socialAccountRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void returnsExistingUserForLinkedKakaoAccount() {
        User existing = User.reconstitute(new UserId(1L), "기존 사용자", UserStatus.ACTIVE);
        when(socialAccountRepository.findUser(SocialProvider.KAKAO, "kakao-1")).thenReturn(Optional.of(existing));

        ProvisionedUser result = service.provisionKakaoUser("kakao-1", "새 닉네임");

        assertThat(result).isEqualTo(new ProvisionedUser(1L, "기존 사용자", false));
        verify(userRepository, never()).save(any());
        verify(socialAccountRepository, never()).save(any());
    }

    @Test
    void createsPendingUserAndLinksFirstKakaoLogin() {
        when(socialAccountRepository.findUser(SocialProvider.KAKAO, "kakao-1")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenReturn(User.reconstitute(new UserId(2L), "카카오 사용자", UserStatus.PENDING_ONBOARDING));

        ProvisionedUser result = service.provisionKakaoUser("kakao-1", "카카오 사용자");

        assertThat(result).isEqualTo(new ProvisionedUser(2L, "카카오 사용자", true));
        verify(socialAccountRepository).save(SocialAccount.link(new UserId(2L), SocialProvider.KAKAO, "kakao-1"));
    }
}
