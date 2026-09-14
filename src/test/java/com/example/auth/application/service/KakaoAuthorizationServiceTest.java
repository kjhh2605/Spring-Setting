package com.example.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.auth.application.port.in.command.dto.KakaoAuthorizationInfo;
import com.example.auth.application.port.out.KakaoAuthorizationUrlProvider;
import com.example.auth.application.port.out.OAuthLoginRequestStore;
import com.example.auth.application.port.out.OAuthValueGenerator;
import com.example.auth.domain.OAuthLoginRequest;
import com.example.auth.domain.OAuthPolicy;

@ExtendWith(MockitoExtension.class)
class KakaoAuthorizationServiceTest {

    @Mock
    private OAuthValueGenerator valueGenerator;

    @Mock
    private OAuthLoginRequestStore requestStore;

    @Mock
    private KakaoAuthorizationUrlProvider urlProvider;

    @Test
    void createsPkceAuthorizationRequestAndStoresOneTimeValues() {
        when(valueGenerator.generate()).thenReturn("state", "nonce", "code-verifier");
        when(urlProvider.create("state", "nonce", "qdgLLRr1saFHT6DWfWU28VNPIi7e9ynEBnBG3Oadw9g"))
                .thenReturn("https://kauth.kakao.com/oauth/authorize?state=state");
        KakaoAuthorizationService service = new KakaoAuthorizationService(
                valueGenerator, requestStore, urlProvider, new OAuthPolicy(Duration.ofMinutes(5)));

        KakaoAuthorizationInfo result = service.create();

        assertThat(result.authorizationUrl()).contains("kauth.kakao.com");
        verify(requestStore).save("state", new OAuthLoginRequest("nonce", "code-verifier"), Duration.ofMinutes(5));
    }
}
