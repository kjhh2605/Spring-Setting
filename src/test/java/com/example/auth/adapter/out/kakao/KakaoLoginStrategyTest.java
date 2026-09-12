package com.example.auth.adapter.out.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.example.auth.application.error.AuthErrorCode;
import com.example.shared.error.BusinessException;

class KakaoLoginStrategyTest {
    private final KakaoProperties properties = new KakaoProperties(
            123L,
            URI.create("https://kakao.test/token"),
            URI.create("https://kakao.test/user"),
            Duration.ofSeconds(1),
            Duration.ofSeconds(1));
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server =
            MockRestServiceServer.bindTo(builder).build();
    private final KakaoLoginStrategy strategy = new KakaoLoginStrategy(properties, builder.build());

    @Test
    void checksAppAndUserIdentityBeforeReturningProfile() {
        tokenResponse("{\"id\":42,\"app_id\":123,\"expires_in\":100}");
        server.expect(requestTo(properties.userInfoUri()))
                .andExpect(header("Authorization", "Bearer sdk-token"))
                .andRespond(
                        withSuccess("{\"id\":42,\"properties\":{\"nickname\":\"사용자\"}}", MediaType.APPLICATION_JSON));
        var identity = strategy.verify("sdk-token");
        assertThat(identity.provider()).isEqualTo("kakao");
        assertThat(identity.subject()).isEqualTo("42");
        assertThat(identity.displayName()).isEqualTo("사용자");
        server.verify();
    }

    @Test
    void rejectsTokensIssuedToAnotherAppWithoutLookingUpUser() {
        tokenResponse("{\"id\":42,\"app_id\":999,\"expires_in\":100}");
        assertInvalid();
        server.verify();
    }

    @Test
    void rejectsExpiredToken() {
        tokenResponse("{\"id\":42,\"app_id\":123,\"expires_in\":0}");
        assertInvalid();
    }

    @Test
    void rejectsMismatchedUser() {
        tokenResponse("{\"id\":42,\"app_id\":123,\"expires_in\":100}");
        server.expect(requestTo(properties.userInfoUri()))
                .andRespond(withSuccess("{\"id\":43}", MediaType.APPLICATION_JSON));
        assertInvalid();
    }

    @Test
    void classifiesProviderOutageSeparately() {
        server.expect(requestTo(properties.tokenInfoUri())).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        assertThatThrownBy(() -> strategy.verify("sdk-token"))
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode())
                        .isEqualTo(AuthErrorCode.SOCIAL_PROVIDER_UNAVAILABLE));
    }

    private void tokenResponse(String json) {
        server.expect(requestTo(properties.tokenInfoUri()))
                .andExpect(header("Authorization", "Bearer sdk-token"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    private void assertInvalid() {
        assertThatThrownBy(() -> strategy.verify("sdk-token"))
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode())
                        .isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN));
    }
}
