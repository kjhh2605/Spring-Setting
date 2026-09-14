package com.example.auth.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.auth.domain.RefreshRotation;
import com.example.auth.domain.RefreshSession;
import com.example.support.IntegrationTestSupport;

class RedisRefreshSessionStoreTest extends IntegrationTestSupport {

    @Autowired
    private RedisRefreshSessionStore store;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void rotatesOnceAndRevokesFamilyWhenUsedTokenIsReplayed() {
        RefreshSession session = new RefreshSession(
                1L,
                "session-1",
                "family-1",
                false,
                Instant.now().plusSeconds(3600).truncatedTo(ChronoUnit.SECONDS));
        store.create("current-refresh", session);

        RefreshRotation rotation = store.rotate("current-refresh", "next-refresh");
        RefreshRotation replay = store.rotate("current-refresh", "attacker-refresh");
        RefreshRotation afterReplay = store.rotate("next-refresh", "later-refresh");

        assertThat(rotation.status()).isEqualTo(RefreshRotation.Status.ROTATED);
        assertThat(rotation.rotatedSession()).contains(session);
        assertThat(replay.status()).isEqualTo(RefreshRotation.Status.REUSED);
        assertThat(afterReplay.status()).isEqualTo(RefreshRotation.Status.INVALID);
    }

    @Test
    void revokesCurrentAndAllUserSessions() {
        store.create(
                "refresh-1",
                new RefreshSession(
                        1L, "session-1", "family-1", false, Instant.now().plusSeconds(3600)));
        store.create(
                "refresh-2",
                new RefreshSession(
                        1L, "session-2", "family-2", false, Instant.now().plusSeconds(3600)));

        store.revoke("refresh-1");
        assertThat(store.rotate("refresh-1", "next-1").status()).isEqualTo(RefreshRotation.Status.INVALID);

        store.revokeAll(1L);
        assertThat(store.rotate("refresh-2", "next-2").status()).isEqualTo(RefreshRotation.Status.INVALID);
    }

    @Test
    void revokesOldestSessionWhenUserExceedsConfiguredMaximum() {
        for (int index = 1; index <= 6; index++) {
            store.create(
                    "refresh-" + index,
                    new RefreshSession(
                            1L,
                            "session-0" + index,
                            "family-0" + index,
                            false,
                            Instant.now().plusSeconds(3600)));
        }

        assertThat(store.rotate("refresh-1", "next-1").status()).isEqualTo(RefreshRotation.Status.INVALID);
        assertThat(store.rotate("refresh-6", "next-6").status()).isEqualTo(RefreshRotation.Status.ROTATED);
    }
}
