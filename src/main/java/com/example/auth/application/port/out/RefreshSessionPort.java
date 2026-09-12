package com.example.auth.application.port.out;

import com.example.auth.domain.RefreshIdentity;

public interface RefreshSessionPort {
    void create(RefreshIdentity identity);

    boolean rotate(RefreshIdentity previous, RefreshIdentity next);

    void revoke(RefreshIdentity identity);
}
