package com.example.user.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.support.IntegrationTestSupport;
import com.example.user.SocialUserRegistration;

class SocialUserConcurrencyTest extends IntegrationTestSupport {
    @Autowired
    private SocialUserRegistration users;

    @Test
    void concurrentFirstLoginCreatesOneUserAndProvidersStayDistinct() throws Exception {
        String subject = UUID.randomUUID().toString();
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                start.await();
                return users.findOrRegister("kakao", subject, "첫 사용자");
            });
            var second = executor.submit(() -> {
                start.await();
                return users.findOrRegister("kakao", subject, "두 번째 사용자");
            });
            start.countDown();
            var a = first.get(15, TimeUnit.SECONDS);
            var b = second.get(15, TimeUnit.SECONDS);
            assertThat(a.id()).isEqualTo(b.id());
            assertThat(a.displayName()).isEqualTo(b.displayName());
            assertThat(users.findOrRegister("other-provider", subject, "다른 계정").id())
                    .isNotEqualTo(a.id());
        }
    }
}
