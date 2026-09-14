package com.example.auth.adapter.out.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import com.example.auth.domain.KakaoToken;

class KakaoTokenClientAdapterTest {

    @Test
    void exchangesAuthorizationCodeWithClientSecretAndPkceVerifier() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoProperties properties = properties();
        KakaoTokenClientAdapter adapter = new KakaoTokenClientAdapter(builder.build(), properties);
        LinkedMultiValueMap<String, String> expectedForm = new LinkedMultiValueMap<>();
        expectedForm.add("grant_type", "authorization_code");
        expectedForm.add("client_id", "client-id");
        expectedForm.add("client_secret", "client-secret");
        expectedForm.add("redirect_uri", "https://app.example.com/auth/callback");
        expectedForm.add("code", "authorization-code");
        expectedForm.add("code_verifier", "verifier");
        server.expect(requestTo(properties.tokenUri()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formData(expectedForm))
                .andRespond(withSuccess("{\"id_token\":\"id-token\"}", MediaType.APPLICATION_JSON));

        assertThat(adapter.exchange("authorization-code", "verifier")).isEqualTo(new KakaoToken("id-token"));
        server.verify();
    }

    private KakaoProperties properties() {
        return new KakaoProperties(
                "client-id",
                "client-secret",
                URI.create("https://app.example.com/auth/callback"),
                URI.create("https://kauth.kakao.com/oauth/authorize"),
                URI.create("https://kauth.kakao.com/oauth/token"),
                URI.create("https://kauth.kakao.com"),
                URI.create("https://kauth.kakao.com/.well-known/jwks.json"),
                Duration.ofMinutes(5));
    }
}
