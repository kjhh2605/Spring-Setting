package com.example.auth.adapter.out.redis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import com.example.auth.application.port.out.RefreshSessionStore;
import com.example.auth.domain.RefreshRotation;
import com.example.auth.domain.RefreshSession;

@Repository
class RedisRefreshSessionStore implements RefreshSessionStore {

    private static final String TOKEN_PREFIX = "auth:refresh-token:";
    private static final String FAMILY_PREFIX = "auth:refresh-family:";
    private static final String USER_PREFIX = "auth:user-families:";

    private static final DefaultRedisScript<String> CREATE_SCRIPT = new DefaultRedisScript<>("""
            redis.call('HSET', KEYS[1],
                'status', 'ACTIVE', 'userId', ARGV[1], 'sessionId', ARGV[2],
                'familyId', ARGV[3], 'onboardingRequired', ARGV[4], 'expiresAt', ARGV[5])
            redis.call('EXPIREAT', KEYS[1], ARGV[5])
            redis.call('HSET', KEYS[2],
                'status', 'ACTIVE', 'currentHash', ARGV[6], 'expiresAt', ARGV[5], 'userId', ARGV[1])
            redis.call('EXPIREAT', KEYS[2], ARGV[5])
            redis.call('ZADD', KEYS[3], ARGV[7], ARGV[3])
            redis.call('EXPIREAT', KEYS[3], ARGV[5])
            local overflow = redis.call('ZCARD', KEYS[3]) - tonumber(ARGV[8])
            if overflow > 0 then
                local oldest = redis.call('ZRANGE', KEYS[3], 0, overflow - 1)
                for _, familyId in ipairs(oldest) do
                    local familyKey = ARGV[9] .. familyId
                    if redis.call('EXISTS', familyKey) == 1 then
                        redis.call('HSET', familyKey, 'status', 'REVOKED')
                    end
                    redis.call('ZREM', KEYS[3], familyId)
                end
            end
            return 'CREATED'
            """, String.class);

    private static final DefaultRedisScript<String> ROTATE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return 'INVALID'
            end
            local familyId = redis.call('HGET', KEYS[1], 'familyId')
            local familyKey = ARGV[2] .. familyId
            local tokenStatus = redis.call('HGET', KEYS[1], 'status')
            if tokenStatus == 'USED' then
                if redis.call('EXISTS', familyKey) == 1 then
                    redis.call('HSET', familyKey, 'status', 'REVOKED')
                end
                return 'REUSED'
            end
            if tokenStatus ~= 'ACTIVE' then
                return 'INVALID'
            end
            if redis.call('HGET', familyKey, 'status') ~= 'ACTIVE'
                or redis.call('HGET', familyKey, 'currentHash') ~= ARGV[1] then
                return 'INVALID'
            end
            local userId = redis.call('HGET', KEYS[1], 'userId')
            local sessionId = redis.call('HGET', KEYS[1], 'sessionId')
            local onboardingRequired = redis.call('HGET', KEYS[1], 'onboardingRequired')
            local expiresAt = redis.call('HGET', KEYS[1], 'expiresAt')
            redis.call('HSET', KEYS[1], 'status', 'USED')
            redis.call('HSET', KEYS[2],
                'status', 'ACTIVE', 'userId', userId, 'sessionId', sessionId,
                'familyId', familyId, 'onboardingRequired', onboardingRequired, 'expiresAt', expiresAt)
            redis.call('EXPIREAT', KEYS[2], expiresAt)
            redis.call('HSET', familyKey, 'currentHash', ARGV[3])
            return 'ROTATED|' .. userId .. '|' .. sessionId .. '|' .. familyId
                .. '|' .. onboardingRequired .. '|' .. expiresAt
            """, String.class);

    private static final DefaultRedisScript<String> REVOKE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return 'IGNORED'
            end
            local familyId = redis.call('HGET', KEYS[1], 'familyId')
            local userId = redis.call('HGET', KEYS[1], 'userId')
            local familyKey = ARGV[1] .. familyId
            if redis.call('EXISTS', familyKey) == 1 then
                redis.call('HSET', familyKey, 'status', 'REVOKED')
            end
            redis.call('HSET', KEYS[1], 'status', 'REVOKED')
            redis.call('ZREM', ARGV[2] .. userId, familyId)
            return 'REVOKED'
            """, String.class);

    private static final DefaultRedisScript<String> REVOKE_ALL_SCRIPT = new DefaultRedisScript<>("""
            local families = redis.call('ZRANGE', KEYS[1], 0, -1)
            for _, familyId in ipairs(families) do
                local familyKey = ARGV[1] .. familyId
                if redis.call('EXISTS', familyKey) == 1 then
                    redis.call('HSET', familyKey, 'status', 'REVOKED')
                end
            end
            redis.call('DEL', KEYS[1])
            return 'REVOKED'
            """, String.class);

    private final StringRedisTemplate redisTemplate;
    private final AuthSessionProperties properties;
    private final Clock clock;

    RedisRefreshSessionStore(StringRedisTemplate redisTemplate, AuthSessionProperties properties, Clock clock) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public void create(String refreshToken, RefreshSession session) {
        String tokenHash = hash(refreshToken);
        long expiresAt = session.expiresAt().getEpochSecond();
        redisTemplate.execute(
                CREATE_SCRIPT,
                List.of(tokenKey(tokenHash), familyKey(session.familyId()), userKey(session.userId())),
                Long.toString(session.userId()),
                session.sessionId(),
                session.familyId(),
                Boolean.toString(session.onboardingRequired()),
                Long.toString(expiresAt),
                tokenHash,
                Long.toString(clock.instant().toEpochMilli()),
                Integer.toString(properties.maxPerUser()),
                FAMILY_PREFIX);
    }

    @Override
    public RefreshRotation rotate(String currentRefreshToken, String nextRefreshToken) {
        String currentHash = hash(currentRefreshToken);
        String nextHash = hash(nextRefreshToken);
        String result = redisTemplate.execute(
                ROTATE_SCRIPT,
                List.of(tokenKey(currentHash), tokenKey(nextHash)),
                currentHash,
                FAMILY_PREFIX,
                nextHash);
        if ("INVALID".equals(result)) {
            return RefreshRotation.invalid();
        }
        if ("REUSED".equals(result)) {
            return RefreshRotation.reused();
        }
        return parseRotated(result);
    }

    @Override
    public void revoke(String refreshToken) {
        String tokenHash = hash(refreshToken);
        redisTemplate.execute(REVOKE_SCRIPT, List.of(tokenKey(tokenHash)), FAMILY_PREFIX, USER_PREFIX);
    }

    @Override
    public void revokeAll(long userId) {
        redisTemplate.execute(REVOKE_ALL_SCRIPT, List.of(userKey(userId)), FAMILY_PREFIX);
    }

    private static RefreshRotation parseRotated(String result) {
        if (result == null || !result.startsWith("ROTATED|")) {
            throw new IllegalStateException("Unexpected Redis refresh rotation result");
        }
        String[] values = result.split("\\|", -1);
        RefreshSession session = new RefreshSession(
                Long.parseLong(values[1]),
                values[2],
                values[3],
                Boolean.parseBoolean(values[4]),
                Instant.ofEpochSecond(Long.parseLong(values[5])));
        return RefreshRotation.rotated(session);
    }

    private static String tokenKey(String tokenHash) {
        return TOKEN_PREFIX + tokenHash;
    }

    private static String familyKey(String familyId) {
        return FAMILY_PREFIX + familyId;
    }

    private static String userKey(long userId) {
        return USER_PREFIX + userId;
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available", exception);
        }
    }
}
