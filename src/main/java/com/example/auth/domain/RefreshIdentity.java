package com.example.auth.domain;

import java.time.Instant;

public record RefreshIdentity(long userId, String sessionId, String tokenId, Instant expiresAt) {}
