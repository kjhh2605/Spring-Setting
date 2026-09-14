package com.example.auth.adapter.out.redis;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import com.example.auth.application.port.out.OAuthLoginRequestStore;
import com.example.auth.domain.OAuthLoginRequest;

@Repository
class RedisOAuthLoginRequestStore implements OAuthLoginRequestStore {

    private static final String KEY_PREFIX = "auth:kakao-request:";
    private static final DefaultRedisScript<String> CONSUME_SCRIPT = new DefaultRedisScript<>("""
            local value = redis.call('GET', KEYS[1])
            if not value then
                return nil
            end
            redis.call('DEL', KEYS[1])
            return value
            """, String.class);

    private final StringRedisTemplate redisTemplate;

    RedisOAuthLoginRequestStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String state, OAuthLoginRequest request, Duration ttl) {
        redisTemplate.opsForValue().set(key(state), request.nonce() + "|" + request.codeVerifier(), ttl);
    }

    @Override
    public Optional<OAuthLoginRequest> consume(String state) {
        String value = redisTemplate.execute(CONSUME_SCRIPT, List.of(key(state)));
        if (value == null) {
            return Optional.empty();
        }
        String[] parts = value.split("\\|", 2);
        if (parts.length != 2) {
            throw new IllegalStateException("Stored OAuth request is malformed");
        }
        return Optional.of(new OAuthLoginRequest(parts[0], parts[1]));
    }

    private static String key(String state) {
        return KEY_PREFIX + state;
    }
}
