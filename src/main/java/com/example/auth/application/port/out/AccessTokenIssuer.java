package com.example.auth.application.port.out;

public interface AccessTokenIssuer {

    String issue(long userId, String sessionId, boolean onboardingRequired);
}
