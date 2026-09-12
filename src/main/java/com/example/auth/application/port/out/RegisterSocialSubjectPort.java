package com.example.auth.application.port.out;

import com.example.auth.domain.AuthSubject;
import com.example.auth.domain.SocialIdentity;

public interface RegisterSocialSubjectPort {
    AuthSubject findOrRegister(SocialIdentity identity);
}
