package com.example.auth.adapter.out.kakao;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.out.KakaoTokenClient;
import com.example.auth.domain.KakaoToken;
import com.example.shared.error.BusinessException;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
class KakaoTokenClientAdapter implements KakaoTokenClient {

    private final RestClient restClient;
    private final KakaoProperties properties;

    KakaoTokenClientAdapter(@Qualifier("kakaoRestClient") RestClient restClient, KakaoProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public KakaoToken exchange(String authorizationCode, String codeVerifier) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("redirect_uri", properties.redirectUri().toString());
        form.add("code", authorizationCode);
        form.add("code_verifier", codeVerifier);
        try {
            KakaoTokenResponse response = restClient
                    .post()
                    .uri(properties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
            if (response == null) {
                throw new BusinessException(AuthErrorCode.KAKAO_LOGIN_FAILED);
            }
            return new KakaoToken(response.idToken());
        } catch (RestClientException | IllegalArgumentException exception) {
            throw new BusinessException(AuthErrorCode.KAKAO_LOGIN_FAILED, exception);
        }
    }

    private record KakaoTokenResponse(
            @JsonProperty("id_token") String idToken) {}
}
