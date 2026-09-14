package com.example.auth.adapter.out.kakao;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.Test;

class KakaoAuthorizationUrlAdapterTest {

    @Test
    void includesOidcPkceAndRequestCorrelationParameters() {
        KakaoProperties properties = new KakaoProperties(
                "client-id",
                "client-secret",
                URI.create("https://app.example.com/auth/callback"),
                URI.create("https://kauth.kakao.com/oauth/authorize"),
                URI.create("https://kauth.kakao.com/oauth/token"),
                URI.create("https://kauth.kakao.com"),
                URI.create("https://kauth.kakao.com/.well-known/jwks.json"),
                Duration.ofMinutes(5));

        String url = new KakaoAuthorizationUrlAdapter(properties).create("state", "nonce", "challenge");

        assertThat(url)
                .startsWith("https://kauth.kakao.com/oauth/authorize?")
                .contains("response_type=code")
                .contains("scope=openid%20profile_nickname")
                .contains("state=state")
                .contains("nonce=nonce")
                .contains("code_challenge=challenge")
                .contains("code_challenge_method=S256");
    }
}
