package com.example.user;

/** 서버에서 검증을 마친 소셜 식별자만 전달하는 모듈 간 계약. */
public interface SocialUserRegistration {
    UserSummary findOrRegister(String provider, String providerSubject, String displayName);
}
