package com.example.user.application.port.out;

import java.util.Optional;

import com.example.user.domain.SocialAccount;
import com.example.user.domain.SocialProvider;
import com.example.user.domain.User;

public interface SocialAccountRepository {

    Optional<User> findUser(SocialProvider provider, String providerUserId);

    void save(SocialAccount socialAccount);
}
