package com.example.user.application.service;

import java.time.Clock;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.user.ProvisionedUser;
import com.example.user.SocialAccountProvisioning;
import com.example.user.UserRegistered;
import com.example.user.application.port.out.SocialAccountRepository;
import com.example.user.application.port.out.UserRepository;
import com.example.user.domain.SocialAccount;
import com.example.user.domain.SocialProvider;
import com.example.user.domain.User;
import com.example.user.domain.UserId;

@Service
public class SocialAccountProvisioningService implements SocialAccountProvisioning {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public SocialAccountProvisioningService(
            UserRepository userRepository,
            SocialAccountRepository socialAccountRepository,
            ApplicationEventPublisher eventPublisher,
            Clock clock) {
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ProvisionedUser provisionKakaoUser(String providerUserId, String displayName) {
        return socialAccountRepository
                .findUser(SocialProvider.KAKAO, providerUserId)
                .map(SocialAccountProvisioningService::toProvisionedUser)
                .orElseGet(() -> createPendingUser(providerUserId, displayName));
    }

    private ProvisionedUser createPendingUser(String providerUserId, String displayName) {
        User savedUser = userRepository.save(User.registerPending(displayName));
        UserId userId = savedUser.id().orElseThrow(() -> new IllegalStateException("saved user must have an id"));

        socialAccountRepository.save(SocialAccount.link(userId, SocialProvider.KAKAO, providerUserId));
        eventPublisher.publishEvent(new UserRegistered(userId.value(), clock.instant()));
        return toProvisionedUser(savedUser);
    }

    private static ProvisionedUser toProvisionedUser(User user) {
        UserId userId = user.id().orElseThrow(() -> new IllegalStateException("provisioned user must have an id"));
        return new ProvisionedUser(userId.value(), user.displayName(), user.onboardingRequired());
    }
}
