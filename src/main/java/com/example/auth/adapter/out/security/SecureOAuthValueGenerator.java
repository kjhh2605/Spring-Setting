package com.example.auth.adapter.out.security;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.example.auth.application.port.out.OAuthValueGenerator;

@Component
class SecureOAuthValueGenerator implements OAuthValueGenerator {

    private static final int VALUE_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        byte[] value = new byte[VALUE_BYTES];
        secureRandom.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
