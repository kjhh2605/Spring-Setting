package com.example.auth.application.port.out;

import java.time.Duration;
import java.util.Optional;

import com.example.auth.domain.OAuthLoginRequest;

public interface OAuthLoginRequestStore {

    void save(String state, OAuthLoginRequest request, Duration ttl);

    Optional<OAuthLoginRequest> consume(String state);
}
