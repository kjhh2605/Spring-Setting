package com.example.auth.adapter.out.kakao;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.auth.kakao")
public record KakaoProperties(
        Long appId, URI tokenInfoUri, URI userInfoUri, Duration connectTimeout, Duration readTimeout) {
    public KakaoProperties {
        if (appId == null
                || appId <= 0
                || tokenInfoUri == null
                || userInfoUri == null
                || !"https".equals(tokenInfoUri.getScheme())
                || !"https".equals(userInfoUri.getScheme())
                || connectTimeout == null
                || connectTimeout.isNegative()
                || connectTimeout.isZero()
                || readTimeout == null
                || readTimeout.isNegative()
                || readTimeout.isZero()) {
            throw new IllegalArgumentException("invalid app.auth.kakao configuration");
        }
    }
}
