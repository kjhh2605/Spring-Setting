package com.example.auth.application.port.out;

import com.example.auth.domain.RefreshRotation;
import com.example.auth.domain.RefreshSession;

public interface RefreshSessionStore {

    void create(String refreshToken, RefreshSession session);

    RefreshRotation rotate(String currentRefreshToken, String nextRefreshToken);

    void revoke(String refreshToken);

    void revokeAll(long userId);
}
