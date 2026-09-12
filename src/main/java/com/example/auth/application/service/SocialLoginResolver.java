package com.example.auth.application.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.out.SocialLoginStrategy;
import com.example.shared.error.BusinessException;

@Component
public class SocialLoginResolver {
    private final Map<String, SocialLoginStrategy> strategies;

    public SocialLoginResolver(List<SocialLoginStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(SocialLoginStrategy::provider, Function.identity()));
    }

    public SocialLoginStrategy resolve(String provider) {
        // Naver, Google 확장 시 각 SocialLoginStrategy 구현을 Bean으로 등록한다.
        SocialLoginStrategy strategy = strategies.get(provider);
        if (strategy == null) {
            throw new BusinessException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        }
        return strategy;
    }
}
