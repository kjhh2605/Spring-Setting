package com.example.auth.application.port.out;

public interface KakaoAuthorizationUrlProvider {

    String create(String state, String nonce, String codeChallenge);
}
