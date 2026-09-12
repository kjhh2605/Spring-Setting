package com.example.user.application.port.out;

import com.example.user.domain.SocialAccount;

public interface SocialUserRepository {
    SocialUserResult findOrCreate(SocialAccount account);
}
