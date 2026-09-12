package com.example.auth.application.service;

import org.springframework.stereotype.Service;

import com.example.auth.application.port.in.command.LoginUseCase;
import com.example.auth.application.port.in.command.SessionUseCase;
import com.example.auth.application.port.in.command.dto.SocialLoginCommand;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;
import com.example.auth.application.port.out.RegisterSocialSubjectPort;

@Service
public class SocialLoginService implements LoginUseCase {
    private final SocialLoginResolver resolver;
    private final RegisterSocialSubjectPort subjects;
    private final SessionUseCase sessions;

    public SocialLoginService(
            SocialLoginResolver resolver, RegisterSocialSubjectPort subjects, SessionUseCase sessions) {
        this.resolver = resolver;
        this.subjects = subjects;
        this.sessions = sessions;
    }

    @Override
    public TokenPairInfo login(SocialLoginCommand command) {
        var identity = resolver.resolve(command.provider()).verify(command.accessToken());
        var subject = subjects.findOrRegister(identity);
        return sessions.issue(subject.userId());
    }
}
