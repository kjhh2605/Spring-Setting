package com.example.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SocialAccountTest {

    @Test
    void createsKakaoAccountForUser() {
        SocialAccount account = SocialAccount.link(new UserId(1L), SocialProvider.KAKAO, "123456789");

        assertThat(account.userId()).isEqualTo(new UserId(1L));
        assertThat(account.provider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(account.providerUserId()).isEqualTo("123456789");
    }

    @Test
    void rejectsBlankProviderUserId() {
        assertThatThrownBy(() -> SocialAccount.link(new UserId(1L), SocialProvider.KAKAO, " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
