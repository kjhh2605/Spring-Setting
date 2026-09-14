package com.example;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.auth.application.port.out.KakaoIdentityVerifier;
import com.example.auth.application.port.out.KakaoTokenClient;
import com.example.auth.domain.KakaoIdentity;
import com.example.auth.domain.KakaoToken;
import com.example.support.IntegrationTestSupport;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class AuthApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KakaoTokenClient kakaoTokenClient;

    @MockitoBean
    private KakaoIdentityVerifier kakaoIdentityVerifier;

    @Test
    void logsInAndDetectsRefreshTokenReplay() throws Exception {
        String state = createAuthorizationState();
        when(kakaoTokenClient.exchange(eq("authorization-code"), anyString())).thenReturn(new KakaoToken("id-token"));
        when(kakaoIdentityVerifier.verify(eq("id-token"), anyString()))
                .thenReturn(new KakaoIdentity("kakao-123", "카카오 사용자"));

        MvcResult login = mockMvc.perform(post("/api/v1/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorizationCode":"authorization-code","state":"%s"}
                                """.formatted(state)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.result.onboardingRequired").value(true))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andReturn();
        Cookie original = login.getResponse().getCookie("refresh_token");

        MvcResult refresh = mockMvc.perform(post("/api/v1/auth/tokens/refresh").cookie(original))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andReturn();
        Cookie rotated = refresh.getResponse().getCookie("refresh_token");

        mockMvc.perform(post("/api/v1/auth/tokens/refresh").cookie(original))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"));
        mockMvc.perform(post("/api/v1/auth/tokens/refresh").cookie(rotated))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-002"));
    }

    @Test
    void requiresAccessTokenForLogoutAll() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout-all")).andExpect(status().isUnauthorized());
    }

    @Test
    void logsOutAllSessionsForAuthenticatedUser() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout-all").with(jwt().jwt(token -> token.claim("uid", 1L))))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refresh_token", 0));
    }

    @Test
    void rejectsRefreshWithoutCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/tokens/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-002"));
    }

    private String createAuthorizationState() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/kakao/authorization-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.authorizationUrl").isString())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        URI authorizationUri =
                URI.create(body.path("result").path("authorizationUrl").asText());
        return UriComponentsBuilder.fromUri(authorizationUri)
                .build()
                .getQueryParams()
                .getFirst("state");
    }
}
