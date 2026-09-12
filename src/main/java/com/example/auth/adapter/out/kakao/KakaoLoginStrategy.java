package com.example.auth.adapter.out.kakao;

import java.net.http.HttpClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.out.SocialLoginStrategy;
import com.example.auth.domain.SocialIdentity;
import com.example.shared.error.BusinessException;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class KakaoLoginStrategy implements SocialLoginStrategy {
    private final KakaoProperties properties;
    private final RestClient client;

    @Autowired
    public KakaoLoginStrategy(KakaoProperties properties) {
        this(properties, client(properties));
    }

    KakaoLoginStrategy(KakaoProperties properties, RestClient client) {
        this.properties = properties;
        this.client = client;
    }

    @Override
    public String provider() {
        return "kakao";
    }

    @Override
    public SocialIdentity verify(String accessToken) {
        if (accessToken == null || accessToken.isBlank() || accessToken.chars().anyMatch(Character::isWhitespace)) {
            throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
        try {
            var token = client.get()
                    .uri(properties.tokenInfoUri())
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .body(TokenInfo.class);
            if (token == null
                    || !properties.appId().equals(token.appId())
                    || token.id() == null
                    || token.id() <= 0
                    || token.expiresIn() == null
                    || token.expiresIn() <= 0) {
                throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
            }
            var user = client.get()
                    .uri(properties.userInfoUri())
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .body(UserInfo.class);
            if (user == null || !token.id().equals(user.id())) {
                throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
            }
            String nickname =
                    user.properties() == null ? null : user.properties().nickname();
            String displayName = nickname == null || nickname.isBlank() ? "카카오 사용자" : nickname;
            if (displayName.length() > 100) {
                displayName = displayName.substring(0, 100);
            }
            return new SocialIdentity(provider(), token.id().toString(), displayName);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 400) {
                throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
            }
            throw new BusinessException(AuthErrorCode.SOCIAL_PROVIDER_UNAVAILABLE);
        } catch (RestClientException ex) {
            throw new BusinessException(AuthErrorCode.SOCIAL_PROVIDER_UNAVAILABLE);
        }
    }

    private static RestClient client(KakaoProperties properties) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build());
        factory.setReadTimeout(properties.readTimeout());
        return RestClient.builder().requestFactory(factory).build();
    }

    private record TokenInfo(
            Long id,
            @JsonProperty("app_id") Long appId,
            @JsonProperty("expires_in") Long expiresIn) {}

    private record UserInfo(Long id, Properties properties) {
        private record Properties(String nickname) {}
    }
}
