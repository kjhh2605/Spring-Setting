package com.example.auth.adapter.out.kakao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.out.KakaoIdentityVerifier;
import com.example.auth.domain.KakaoIdentity;
import com.example.shared.error.BusinessException;

@Component
class KakaoOidcIdentityVerifier implements KakaoIdentityVerifier {

    private static final String DEFAULT_DISPLAY_NAME = "카카오 사용자";

    private final JwtDecoder jwtDecoder;

    KakaoOidcIdentityVerifier(@Qualifier("kakaoIdTokenDecoder") JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public KakaoIdentity verify(String idToken, String expectedNonce) {
        try {
            Jwt jwt = jwtDecoder.decode(idToken);
            String nonce = jwt.getClaimAsString("nonce");
            if (!matches(nonce, expectedNonce)) {
                throw new BusinessException(AuthErrorCode.KAKAO_LOGIN_FAILED);
            }
            String nickname = jwt.getClaimAsString("nickname");
            return new KakaoIdentity(jwt.getSubject(), hasText(nickname) ? nickname : DEFAULT_DISPLAY_NAME);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(AuthErrorCode.KAKAO_LOGIN_FAILED, exception);
        }
    }

    private static boolean matches(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
