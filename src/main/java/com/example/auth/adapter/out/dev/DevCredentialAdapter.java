package com.example.auth.adapter.out.dev;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.auth.application.port.out.DevCredentialPort;

@Component
@Profile("dev & !prod")
public class DevCredentialAdapter implements DevCredentialPort {
    private final byte[] expected;

    public DevCredentialAdapter(@Value("${app.auth.dev.master-secret}") String secret) {
        if (secret == null || secret.isBlank() || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("dev master-secret must contain at least 32 bytes");
        }
        expected = hash(secret);
    }

    @Override
    public boolean matches(String secret) {
        return secret != null && MessageDigest.isEqual(expected, hash(secret));
    }

    private static byte[] hash(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
