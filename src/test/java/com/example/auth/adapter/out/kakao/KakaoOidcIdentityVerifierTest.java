package com.example.auth.adapter.out.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.example.auth.domain.KakaoIdentity;
import com.example.shared.error.BusinessException;

@ExtendWith(MockitoExtension.class)
class KakaoOidcIdentityVerifierTest {

    @Mock
    private JwtDecoder jwtDecoder;

    @Test
    void verifiesNonceAndMapsSubject() {
        Jwt jwt = new Jwt(
                "id-token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "RS256"),
                Map.of("sub", "12345", "nonce", "expected", "nickname", "카카오 사용자"));
        when(jwtDecoder.decode("id-token")).thenReturn(jwt);

        KakaoIdentity result = new KakaoOidcIdentityVerifier(jwtDecoder).verify("id-token", "expected");

        assertThat(result).isEqualTo(new KakaoIdentity("12345", "카카오 사용자"));
    }

    @Test
    void rejectsMismatchedNonce() {
        Jwt jwt = new Jwt(
                "id-token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "RS256"),
                Map.of("sub", "12345", "nonce", "different"));
        when(jwtDecoder.decode("id-token")).thenReturn(jwt);

        assertThatThrownBy(() -> new KakaoOidcIdentityVerifier(jwtDecoder).verify("id-token", "expected"))
                .isInstanceOf(BusinessException.class);
    }
}
