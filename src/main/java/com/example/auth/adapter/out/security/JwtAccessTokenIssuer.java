package com.example.auth.adapter.out.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.example.auth.application.port.out.AccessTokenIssuer;

class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final Duration accessTokenTtl;
    private final Clock clock;

    JwtAccessTokenIssuer(JwtEncoder jwtEncoder, String issuer, Duration accessTokenTtl, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenTtl = accessTokenTtl;
        this.clock = clock;
    }

    @Override
    public String issue(long userId, String sessionId, boolean onboardingRequired) {
        Instant issuedAt = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject("user:" + userId)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(accessTokenTtl))
                .id(UUID.randomUUID().toString())
                .claim("uid", userId)
                .claim("sid", sessionId)
                .claim("onboarding_required", onboardingRequired)
                .build();
        JwsHeader header = JwsHeader.with(() -> JwsAlgorithms.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
