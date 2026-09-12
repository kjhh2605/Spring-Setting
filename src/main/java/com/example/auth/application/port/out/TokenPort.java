package com.example.auth.application.port.out;

import com.example.auth.application.port.in.command.dto.TokenPairInfo;
import com.example.auth.domain.RefreshIdentity;

public interface TokenPort {
    TokenPairInfo issue(long userId);

    TokenPairInfo rotate(RefreshIdentity previous);

    RefreshIdentity verifyRefresh(String refreshToken);

    RefreshIdentity readIssuedRefresh(TokenPairInfo tokens);
}
