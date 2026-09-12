package com.example.auth.application.port.out;

import com.example.auth.domain.SocialIdentity;

public interface SocialLoginStrategy {
    String provider();

    SocialIdentity verify(String accessToken);
}
