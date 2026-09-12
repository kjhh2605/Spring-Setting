package com.example.auth.adapter.out.token;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;
import com.example.auth.application.port.out.TokenPort;
import com.example.auth.domain.RefreshIdentity;
import com.example.shared.error.BusinessException;

@Component
public class JwtTokenAdapter implements TokenPort {
    private final JwtEncoder encoder;
    private final JwtDecoder refreshDecoder;
    private final TokenProperties properties;
    private final Clock clock;

    public JwtTokenAdapter(
            JwtEncoder encoder,
            @Qualifier("refreshJwtDecoder") JwtDecoder refreshDecoder,
            TokenProperties properties,
            Clock clock) {
        this.encoder = encoder;
        this.refreshDecoder = refreshDecoder;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public TokenPairInfo issue(long userId) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        return pair(userId, UUID.randomUUID().toString(), now, now.plus(properties.refreshTtl()));
    }

    @Override
    public TokenPairInfo rotate(RefreshIdentity previous) {
        return pair(
                previous.userId(),
                previous.sessionId(),
                clock.instant().truncatedTo(ChronoUnit.SECONDS),
                previous.expiresAt());
    }

    @Override
    public RefreshIdentity verifyRefresh(String refreshToken) {
        try {
            var jwt = refreshDecoder.decode(refreshToken);
            long userId = Long.parseLong(jwt.getSubject().substring("user:".length()));
            String sid = UUID.fromString(jwt.getClaimAsString("sid")).toString();
            String tokenId = UUID.fromString(jwt.getId()).toString();
            if (userId <= 0 || !clock.instant().isBefore(jwt.getExpiresAt())) {
                throw new IllegalArgumentException("expired refresh token");
            }
            return new RefreshIdentity(userId, sid, tokenId, jwt.getExpiresAt());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    @Override
    public RefreshIdentity readIssuedRefresh(TokenPairInfo tokens) {
        return verifyRefresh(tokens.refreshToken());
    }

    private TokenPairInfo pair(long userId, String sessionId, Instant now, Instant refreshExpiry) {
        Instant accessExpiry = now.plus(properties.accessTtl());
        if (accessExpiry.isAfter(refreshExpiry)) {
            accessExpiry = refreshExpiry;
        }
        return new TokenPairInfo(
                encode(userId, sessionId, now, accessExpiry, properties.accessAudience(), "access"),
                encode(userId, sessionId, now, refreshExpiry, properties.refreshAudience(), "refresh"),
                accessExpiry,
                refreshExpiry);
    }

    private String encode(long userId, String sessionId, Instant now, Instant expiry, String audience, String use) {
        var claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject("user:" + userId)
                .audience(List.of(audience))
                .issuedAt(now)
                .expiresAt(expiry)
                .id(UUID.randomUUID().toString())
                .claim("sid", sessionId)
                .claim("token_use", use)
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
