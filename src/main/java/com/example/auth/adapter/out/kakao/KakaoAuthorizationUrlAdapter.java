package com.example.auth.adapter.out.kakao;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.auth.application.port.out.KakaoAuthorizationUrlProvider;

@Component
class KakaoAuthorizationUrlAdapter implements KakaoAuthorizationUrlProvider {

    private final KakaoProperties properties;

    KakaoAuthorizationUrlAdapter(KakaoProperties properties) {
        this.properties = properties;
    }

    @Override
    public String create(String state, String nonce, String codeChallenge) {
        return UriComponentsBuilder.fromUri(properties.authorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.clientId())
                .queryParam("redirect_uri", properties.redirectUri())
                .queryParam("scope", "openid profile_nickname")
                .queryParam("state", state)
                .queryParam("nonce", nonce)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build()
                .encode()
                .toUriString();
    }
}
