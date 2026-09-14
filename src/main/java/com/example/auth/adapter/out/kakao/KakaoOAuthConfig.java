package com.example.auth.adapter.out.kakao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.auth.domain.OAuthPolicy;

@Configuration
class KakaoOAuthConfig {

    @Bean
    OAuthPolicy oAuthPolicy(KakaoProperties properties) {
        return new OAuthPolicy(properties.stateTtl());
    }
}
