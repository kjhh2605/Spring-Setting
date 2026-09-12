package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.auth.adapter.out.kakao.KakaoLoginStrategy;
import com.example.auth.domain.SocialIdentity;
import com.example.support.IntegrationTestSupport;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class AuthLoginIntegrationTest extends IntegrationTestSupport {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoSpyBean
    private KakaoLoginStrategy kakao;

    @Test
    void logsInRotatesRejectsReplayAndLogsOut() throws Exception {
        // resolver는 context 시작 시 provider를 수집하므로 아래 mock은 provider 메서드를 유지해야 한다.
        var first = login();
        String refresh = first.path("refreshToken").asString();
        String access = first.path("accessToken").asString();
        mvc.perform(get("/requires-authentication").header("Authorization", "Bearer " + access))
                .andExpect(status().isNotFound());
        mvc.perform(get("/requires-authentication").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());

        JsonNode rotated = mapper.readTree(mvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body(refresh)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray())
                .path("result");
        assertThat(rotated.path("refreshToken").asString()).isNotEqualTo(refresh);
        assertThat(rotated.path("refreshTokenExpiresAt")).isEqualTo(first.path("refreshTokenExpiresAt"));
        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(refresh)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-005"));
        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(rotated.path("refreshToken").asString())))
                .andExpect(status().isUnauthorized());

        var another = login();
        String anotherRefresh = another.path("refreshToken").asString();
        mvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(anotherRefresh)))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(anotherRefresh)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUnsupportedProvidersAndEmptyTokensAndHidesDevApi() throws Exception {
        mvc.perform(post("/api/v1/auth/social/naver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessToken\":\"sdk-token\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH-002"));
        mvc.perform(post("/api/v1/auth/social/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessToken\":\" \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/dev/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode login() throws Exception {
        doReturn(new SocialIdentity("kakao", "integration-42", "소셜 사용자"))
                .when(kakao)
                .verify(anyString());
        return mapper.readTree(mvc.perform(post("/api/v1/auth/social/kakao")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"accessToken\":\"sdk-token\"}"))
                        .andExpect(status().isOk())
                        .andExpect(header().string("Cache-Control", "no-store"))
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray())
                .path("result");
    }

    private String body(String refresh) {
        return "{\"refreshToken\":\"" + refresh + "\"}";
    }
}
