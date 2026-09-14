package com.example.auth.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.auth.domain.OAuthLoginRequest;
import com.example.support.IntegrationTestSupport;

class RedisOAuthLoginRequestStoreTest extends IntegrationTestSupport {

    @Autowired
    private RedisOAuthLoginRequestStore store;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void consumesLoginRequestOnlyOnce() {
        store.save("state", new OAuthLoginRequest("nonce", "verifier"), Duration.ofMinutes(5));

        assertThat(store.consume("state")).contains(new OAuthLoginRequest("nonce", "verifier"));
        assertThat(store.consume("state")).isEmpty();
    }
}
