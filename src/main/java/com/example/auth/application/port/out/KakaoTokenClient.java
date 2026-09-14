package com.example.auth.application.port.out;

import com.example.auth.domain.KakaoToken;

public interface KakaoTokenClient {

    KakaoToken exchange(String authorizationCode, String codeVerifier);
}
