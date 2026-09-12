package com.example.auth.adapter.out.token;

import java.time.Clock;
import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration(proxyBeanMethods = false)
public class AuthTokenConfiguration {
    @Bean
    JwtEncoder jwtEncoder(TokenProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key(properties)));
    }

    @Bean
    @Primary
    JwtDecoder accessJwtDecoder(TokenProperties properties, Clock clock) {
        return decoder(properties, clock, properties.accessAudience(), "access");
    }

    @Bean
    JwtDecoder refreshJwtDecoder(TokenProperties properties, Clock clock) {
        return decoder(properties, clock, properties.refreshAudience(), "refresh");
    }

    static NimbusJwtDecoder decoder(TokenProperties properties, Clock clock, String audience, String use) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key(properties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtTimestampValidator timestamp = new JwtTimestampValidator(properties.clockSkew());
        timestamp.setClock(clock);
        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(timestamp, new JwtIssuerValidator(properties.issuer()), jwt -> {
                    boolean valid = jwt.getExpiresAt() != null
                            && jwt.getIssuedAt() != null
                            && jwt.getAudience().equals(java.util.List.of(audience))
                            && use.equals(jwt.getClaimAsString("token_use"))
                            && jwt.getSubject() != null
                            && jwt.getSubject().matches("user:[1-9][0-9]*")
                            && jwt.getId() != null
                            && jwt.getClaimAsString("sid") != null;
                    return valid
                            ? OAuth2TokenValidatorResult.success()
                            : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
                }));
        return decoder;
    }

    private static SecretKeySpec key(TokenProperties properties) {
        return new SecretKeySpec(Base64.getDecoder().decode(properties.secret()), "HmacSHA256");
    }
}
