package com.example.auth.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.auth.domain.RefreshIdentity;

@Testcontainers(disabledWithoutDocker = true)
class RedisRefreshSessionAdapterTest {
    @Container
    private static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    private LettuceConnectionFactory connection;
    private StringRedisTemplate redis;
    private RedisRefreshSessionAdapter adapter;

    @BeforeEach
    void setUp() {
        connection = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connection.afterPropertiesSet();
        redis = new StringRedisTemplate(connection);
        adapter = new RedisRefreshSessionAdapter(redis, new RefreshStoreProperties("test:refresh:"), Clock.systemUTC());
    }

    @AfterEach
    void close() {
        connection.destroy();
    }

    @Test
    void rotatesOncePreservesTtlAndRevokesFamilyOnReuse() {
        var first = identity();
        var next = next(first);
        var otherSession = identity();
        adapter.create(first);
        adapter.create(otherSession);
        long before = redis.getExpire(key(first), TimeUnit.MILLISECONDS);

        assertThat(adapter.rotate(first, next)).isTrue();
        assertThat(redis.getExpire(key(first), TimeUnit.MILLISECONDS))
                .isPositive()
                .isLessThanOrEqualTo(before);
        assertThat(redis.opsForValue().get(key(first))).doesNotContain(first.tokenId(), next.tokenId());
        assertThat(adapter.rotate(first, next(first))).isFalse();
        assertThat(adapter.rotate(next, next(next))).isFalse();
        assertThat(adapter.rotate(otherSession, next(otherSession))).isTrue();
    }

    @Test
    void simultaneousReuseAllowsOnlyOneRotationThenRevokesSession() throws Exception {
        var first = identity();
        var second = next(first);
        var third = next(first);
        adapter.create(first);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var a = executor.submit(() -> {
                start.await();
                return adapter.rotate(first, second);
            });
            var b = executor.submit(() -> {
                start.await();
                return adapter.rotate(first, third);
            });
            start.countDown();
            boolean resultA = a.get(10, TimeUnit.SECONDS);
            boolean resultB = b.get(10, TimeUnit.SECONDS);
            assertThat(resultA ^ resultB).isTrue();
        }
        assertThat(redis.hasKey(key(first))).isFalse();
    }

    @Test
    void rejectsMissingAndLoggedOutSessions() {
        var first = identity();
        assertThat(adapter.rotate(first, next(first))).isFalse();
        adapter.create(first);
        adapter.revoke(first);
        adapter.revoke(first);
        assertThat(adapter.rotate(first, next(first))).isFalse();
    }

    private RefreshIdentity identity() {
        return new RefreshIdentity(
                1,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                Instant.now().plusSeconds(60));
    }

    private RefreshIdentity next(RefreshIdentity previous) {
        return new RefreshIdentity(
                previous.userId(), previous.sessionId(), UUID.randomUUID().toString(), previous.expiresAt());
    }

    private String key(RefreshIdentity identity) {
        return "test:refresh:{" + identity.sessionId() + "}";
    }
}
