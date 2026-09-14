package com.example.auth.application.port.out;

import com.example.auth.domain.KakaoIdentity;

public interface KakaoIdentityVerifier {

    KakaoIdentity verify(String idToken, String expectedNonce);
}
