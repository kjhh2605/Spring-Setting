package com.example.user;

public interface SocialAccountProvisioning {

    ProvisionedUser provisionKakaoUser(String providerUserId, String displayName);
}
