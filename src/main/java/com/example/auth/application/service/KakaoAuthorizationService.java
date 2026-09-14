package com.example.auth.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.example.auth.application.port.in.command.CreateKakaoAuthorizationUseCase;
import com.example.auth.application.port.in.command.dto.KakaoAuthorizationInfo;
import com.example.auth.application.port.out.KakaoAuthorizationUrlProvider;
import com.example.auth.application.port.out.OAuthLoginRequestStore;
import com.example.auth.application.port.out.OAuthValueGenerator;
import com.example.auth.domain.OAuthLoginRequest;
import com.example.auth.domain.OAuthPolicy;

@Service
public class KakaoAuthorizationService implements CreateKakaoAuthorizationUseCase {

    private final OAuthValueGenerator valueGenerator;
    private final OAuthLoginRequestStore requestStore;
    private final KakaoAuthorizationUrlProvider urlProvider;
    private final OAuthPolicy policy;

    public KakaoAuthorizationService(
            OAuthValueGenerator valueGenerator,
            OAuthLoginRequestStore requestStore,
            KakaoAuthorizationUrlProvider urlProvider,
            OAuthPolicy policy) {
        this.valueGenerator = valueGenerator;
        this.requestStore = requestStore;
        this.urlProvider = urlProvider;
        this.policy = policy;
    }

    @Override
    public KakaoAuthorizationInfo create() {
        String state = valueGenerator.generate();
        String nonce = valueGenerator.generate();
        String codeVerifier = valueGenerator.generate();
        String codeChallenge = codeChallenge(codeVerifier);
        requestStore.save(state, new OAuthLoginRequest(nonce, codeVerifier), policy.stateTtl());
        return new KakaoAuthorizationInfo(urlProvider.create(state, nonce, codeChallenge));
    }

    private static String codeChallenge(String codeVerifier) {
        try {
            byte[] digest =
                    MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available", exception);
        }
    }
}
