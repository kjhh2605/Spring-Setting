package com.example.auth.application.service;

import org.springframework.stereotype.Service;

import com.example.auth.application.error.AuthErrorCode;
import com.example.auth.application.port.in.command.IssueTokenPairUseCase;
import com.example.auth.application.port.in.command.LoginWithKakaoUseCase;
import com.example.auth.application.port.in.command.dto.KakaoLoginCommand;
import com.example.auth.application.port.in.command.dto.TokenPairInfo;
import com.example.auth.application.port.out.KakaoIdentityVerifier;
import com.example.auth.application.port.out.KakaoTokenClient;
import com.example.auth.application.port.out.OAuthLoginRequestStore;
import com.example.auth.application.port.out.ProvisionLoginUserPort;
import com.example.auth.domain.KakaoIdentity;
import com.example.auth.domain.KakaoToken;
import com.example.auth.domain.LoginUser;
import com.example.auth.domain.OAuthLoginRequest;
import com.example.shared.error.BusinessException;

@Service
public class LoginWithKakaoService implements LoginWithKakaoUseCase {

    private final OAuthLoginRequestStore requestStore;
    private final KakaoTokenClient tokenClient;
    private final KakaoIdentityVerifier identityVerifier;
    private final ProvisionLoginUserPort provisionLoginUserPort;
    private final IssueTokenPairUseCase issueTokenPairUseCase;

    public LoginWithKakaoService(
            OAuthLoginRequestStore requestStore,
            KakaoTokenClient tokenClient,
            KakaoIdentityVerifier identityVerifier,
            ProvisionLoginUserPort provisionLoginUserPort,
            IssueTokenPairUseCase issueTokenPairUseCase) {
        this.requestStore = requestStore;
        this.tokenClient = tokenClient;
        this.identityVerifier = identityVerifier;
        this.provisionLoginUserPort = provisionLoginUserPort;
        this.issueTokenPairUseCase = issueTokenPairUseCase;
    }

    @Override
    public TokenPairInfo login(KakaoLoginCommand command) {
        OAuthLoginRequest request = requestStore
                .consume(command.state())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.OAUTH_REQUEST_INVALID));
        KakaoToken kakaoToken = tokenClient.exchange(command.authorizationCode(), request.codeVerifier());
        KakaoIdentity identity = identityVerifier.verify(kakaoToken.idToken(), request.nonce());
        LoginUser user = provisionLoginUserPort.provision(identity.providerUserId(), identity.displayName());
        return issueTokenPairUseCase.issue(user.userId(), user.onboardingRequired());
    }
}
