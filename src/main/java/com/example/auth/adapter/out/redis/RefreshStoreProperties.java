package com.example.auth.adapter.out.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.auth.refresh-store")
public record RefreshStoreProperties(String keyPrefix) {
    public RefreshStoreProperties {
        if (keyPrefix == null || keyPrefix.isBlank() || keyPrefix.contains("{") || keyPrefix.contains("}")) {
            throw new IllegalArgumentException("invalid refresh-store key-prefix");
        }
    }
}
