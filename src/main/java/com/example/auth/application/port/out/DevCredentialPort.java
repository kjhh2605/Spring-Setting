package com.example.auth.application.port.out;

public interface DevCredentialPort {
    boolean matches(String secret);
}
