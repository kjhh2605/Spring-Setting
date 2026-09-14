package com.example.user.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.support.TestcontainersConfiguration;
import com.example.user.domain.SocialAccount;
import com.example.user.domain.SocialProvider;
import com.example.user.domain.User;
import com.example.user.domain.UserId;
import com.example.user.domain.UserStatus;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class UserPersistenceAdapterTest {

    @Autowired
    private SpringDataUserRepository springDataRepository;

    @Autowired
    private SpringDataSocialAccountRepository springDataSocialAccountRepository;

    @Test
    void savesAndRestoresDomainUser() {
        UserPersistenceAdapter adapter = new UserPersistenceAdapter(springDataRepository);

        User saved = adapter.save(User.register("홍길동"));

        assertThat(saved.id()).isPresent();
        assertThat(adapter.findById(saved.id().orElseThrow()))
                .get()
                .extracting(User::displayName)
                .isEqualTo("홍길동");
    }

    @Test
    void savesPendingUserAndFindsItThroughSocialAccount() {
        UserPersistenceAdapter userAdapter = new UserPersistenceAdapter(springDataRepository);
        SocialAccountPersistenceAdapter socialAccountAdapter =
                new SocialAccountPersistenceAdapter(springDataSocialAccountRepository, springDataRepository);
        User saved = userAdapter.save(User.registerPending("카카오 사용자"));
        UserId userId = saved.id().orElseThrow();

        socialAccountAdapter.save(SocialAccount.link(userId, SocialProvider.KAKAO, "kakao-1"));

        assertThat(socialAccountAdapter.findUser(SocialProvider.KAKAO, "kakao-1"))
                .get()
                .satisfies(user -> {
                    assertThat(user.id()).contains(userId);
                    assertThat(user.status()).isEqualTo(UserStatus.PENDING_ONBOARDING);
                });
    }
}
