package com.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.example.support.IntegrationTestSupport;

import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
@TestPropertySource(
        properties = {
            "DEV_MASTER_SECRET=development-only-test-secret-32bytes",
            "DATABASE_URL=jdbc:postgresql://unused/test",
            "DATABASE_USERNAME=test",
            "DATABASE_PASSWORD=test"
        })
class DevTokenIntegrationTest extends IntegrationTestSupport {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void issuesOrdinaryRotatableTokensForExistingUserOnly() throws Exception {
        var registered = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"개발 사용자\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long id = mapper.readTree(registered.getResponse().getContentAsByteArray())
                .path("result")
                .path("id")
                .asLong();
        var issued = mvc.perform(post("/api/v1/auth/dev/tokens")
                        .header("X-Dev-Master-Key", "development-only-test-secret-32bytes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + id + "}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.result.tokenType").value("Bearer"))
                .andReturn();
        String refresh = mapper.readTree(issued.getResponse().getContentAsByteArray())
                .path("result")
                .path("refreshToken")
                .asString();
        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsWrongSecretAndMissingUser() throws Exception {
        mvc.perform(post("/api/v1/auth/dev/tokens")
                        .header("X-Dev-Master-Key", "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-006"));
        mvc.perform(post("/api/v1/auth/dev/tokens")
                        .header("X-Dev-Master-Key", "development-only-test-secret-32bytes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":9223372036854775807}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AUTH-001"));
    }
}
