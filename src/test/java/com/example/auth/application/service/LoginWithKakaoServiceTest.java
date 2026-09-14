package com.example.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.auth.application.port.in.command.IssueTokenPairUseCase;
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

@ExtendWith(MockitoExtension.class)
class LoginWithKakaoServiceTest {

    @Mock
    private OAuthLoginRequestStore requestStore;

    @Mock
    private KakaoTokenClient tokenClient;

    @Mock
    private KakaoIdentityVerifier identityVerifier;

    @Mock
    private ProvisionLoginUserPort provisionLoginUserPort;

    @Mock
    private IssueTokenPairUseCase issueTokenPairUseCase;

    @Test
    void exchangesVerifiedKakaoIdentityForServiceTokens() {
        OAuthLoginRequest request = new OAuthLoginRequest("nonce", "verifier");
        when(requestStore.consume("state")).thenReturn(Optional.of(request));
        when(tokenClient.exchange("authorization-code", "verifier")).thenReturn(new KakaoToken("id-token"));
        when(identityVerifier.verify("id-token", "nonce")).thenReturn(new KakaoIdentity("kakao-123", "카카오 사용자"));
        when(provisionLoginUserPort.provision("kakao-123", "카카오 사용자")).thenReturn(new LoginUser(1L, true));
        TokenPairInfo expected = new TokenPairInfo("access", "refresh", Duration.ofDays(14), true);
        when(issueTokenPairUseCase.issue(1L, true)).thenReturn(expected);
        LoginWithKakaoService service = service();

        TokenPairInfo result = service.login(new KakaoLoginCommand("authorization-code", "state"));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void rejectsMissingOrAlreadyConsumedState() {
        when(requestStore.consume("invalid-state")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().login(new KakaoLoginCommand("code", "invalid-state")))
                .isInstanceOf(BusinessException.class);
    }

    private LoginWithKakaoService service() {
        return new LoginWithKakaoService(
                requestStore, tokenClient, identityVerifier, provisionLoginUserPort, issueTokenPairUseCase);
    }
}
