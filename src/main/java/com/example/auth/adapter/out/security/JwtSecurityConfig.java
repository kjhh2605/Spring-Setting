package com.example.auth.adapter.out.security;

import java.time.Clock;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.example.auth.application.port.out.AccessTokenIssuer;
import com.example.auth.domain.TokenPolicy;

@Configuration
class JwtSecurityConfig {

    private static final int MINIMUM_SECRET_BYTES = 32;

    @Bean
    TokenPolicy tokenPolicy(AuthTokenProperties properties) {
        return new TokenPolicy(properties.accessTokenTtl(), properties.refreshTokenTtl());
    }

    @Bean
    JwtEncoder jwtEncoder(AuthTokenProperties properties) {
        return NimbusJwtEncoder.withSecretKey(secretKey(properties.secret()))
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    @Primary
    JwtDecoder jwtDecoder(AuthTokenProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(properties.secret()))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<Jwt>(
                new JwtTimestampValidator(properties.clockSkew()), new JwtIssuerValidator(properties.issuer())));
        return decoder;
    }

    @Bean
    AccessTokenIssuer accessTokenIssuer(JwtEncoder jwtEncoder, AuthTokenProperties properties, Clock clock) {
        return new JwtAccessTokenIssuer(jwtEncoder, properties.issuer(), properties.accessTokenTtl(), clock);
    }

    static SecretKey secretKey(String encodedSecret) {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(encodedSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT secret must be Base64 encoded", exception);
        }
        if (decoded.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException("JWT secret must contain at least 32 bytes");
        }
        return new SecretKeySpec(decoded, "HmacSHA256");
    }
}
