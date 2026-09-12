package com.example.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.auth.application.port.out.SocialLoginStrategy;
import com.example.shared.error.BusinessException;

class SocialLoginResolverTest {
    @Test
    void selectsRegisteredProviderAndRejectsUnsupportedProvider() {
        SocialLoginStrategy kakao = mock(SocialLoginStrategy.class);
        when(kakao.provider()).thenReturn("kakao");
        SocialLoginResolver resolver = new SocialLoginResolver(List.of(kakao));

        assertThat(resolver.resolve("kakao")).isSameAs(kakao);
        assertThatThrownBy(() -> resolver.resolve("naver")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> resolver.resolve("google")).isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsDuplicateStrategiesAtStartup() {
        SocialLoginStrategy first = mock(SocialLoginStrategy.class);
        SocialLoginStrategy second = mock(SocialLoginStrategy.class);
        when(first.provider()).thenReturn("kakao");
        when(second.provider()).thenReturn("kakao");
        assertThatThrownBy(() -> new SocialLoginResolver(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class);
    }
}
