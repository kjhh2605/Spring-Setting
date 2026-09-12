package com.example.auth.adapter.out.user;

import org.springframework.stereotype.Component;

import com.example.auth.application.port.out.RegisterSocialSubjectPort;
import com.example.auth.domain.AuthSubject;
import com.example.auth.domain.SocialIdentity;
import com.example.user.SocialUserRegistration;

@Component
public class SocialSubjectAdapter implements RegisterSocialSubjectPort {
    private final SocialUserRegistration users;

    public SocialSubjectAdapter(SocialUserRegistration users) {
        this.users = users;
    }

    @Override
    public AuthSubject findOrRegister(SocialIdentity identity) {
        return new AuthSubject(users.findOrRegister(identity.provider(), identity.subject(), identity.displayName())
                .id());
    }
}
