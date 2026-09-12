package com.example.user.application.service;

import java.time.Clock;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.user.SocialUserRegistration;
import com.example.user.UserRegistered;
import com.example.user.UserSummary;
import com.example.user.application.port.out.SocialUserRepository;
import com.example.user.domain.SocialAccount;

@Service
public class SocialUserService implements SocialUserRegistration {
    private final SocialUserRepository repository;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public SocialUserService(SocialUserRepository repository, ApplicationEventPublisher events, Clock clock) {
        this.repository = repository;
        this.events = events;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UserSummary findOrRegister(String provider, String providerSubject, String displayName) {
        var result = repository.findOrCreate(new SocialAccount(provider, providerSubject, displayName));
        var user = result.user();
        long userId = user.id().orElseThrow().value();
        if (result.created()) {
            events.publishEvent(new UserRegistered(userId, clock.instant()));
        }
        return new UserSummary(userId, user.displayName());
    }
}
