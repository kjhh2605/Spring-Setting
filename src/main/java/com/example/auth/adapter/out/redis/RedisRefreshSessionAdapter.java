package com.example.auth.adapter.out.redis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.out.RefreshSessionPort;
import com.example.auth.domain.RefreshIdentity;
import com.example.shared.error.BusinessException;

@Component
public class RedisRefreshSessionAdapter implements RefreshSessionPort {
    private static final DefaultRedisScript<Long> ROTATE = new DefaultRedisScript<>("""
            local current = redis.call('GET', KEYS[1])
            if not current then return 0 end
            if current ~= ARGV[1] then
                redis.call('DEL', KEYS[1])
                return -1
            end
            redis.call('SET', KEYS[1], ARGV[2], 'KEEPTTL')
            return 1
            """, Long.class);

    private final StringRedisTemplate redis;
    private final RefreshStoreProperties properties;
    private final Clock clock;

    public RedisRefreshSessionAdapter(StringRedisTemplate redis, RefreshStoreProperties properties, Clock clock) {
        this.redis = redis;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public void create(RefreshIdentity identity) {
        try {
            Duration ttl = Duration.between(clock.instant(), identity.expiresAt());
            if (ttl.isNegative()
                    || ttl.isZero()
                    || !Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key(identity), digest(identity), ttl))) {
                throw new BusinessException(AuthErrorCode.SESSION_STORE_UNAVAILABLE);
            }
        } catch (DataAccessException ex) {
            throw new BusinessException(AuthErrorCode.SESSION_STORE_UNAVAILABLE);
        }
    }

    @Override
    public boolean rotate(RefreshIdentity previous, RefreshIdentity next) {
        if (previous.userId() != next.userId()
                || !previous.sessionId().equals(next.sessionId())
                || !previous.expiresAt().equals(next.expiresAt())) {
            throw new IllegalArgumentException("rotation must preserve session identity and expiry");
        }
        try {
            return Long.valueOf(1)
                    .equals(redis.execute(ROTATE, List.of(key(previous)), digest(previous), digest(next)));
        } catch (DataAccessException ex) {
            throw new BusinessException(AuthErrorCode.SESSION_STORE_UNAVAILABLE);
        }
    }

    @Override
    public void revoke(RefreshIdentity identity) {
        try {
            redis.delete(key(identity));
        } catch (DataAccessException ex) {
            throw new BusinessException(AuthErrorCode.SESSION_STORE_UNAVAILABLE);
        }
    }

    private String key(RefreshIdentity identity) {
        return properties.keyPrefix() + "{" + identity.sessionId() + "}";
    }

    private String digest(RefreshIdentity identity) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256")
                            .digest((identity.userId() + ":" + identity.tokenId()).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
