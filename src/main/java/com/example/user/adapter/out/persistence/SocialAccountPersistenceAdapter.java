package com.example.user.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.user.application.port.out.SocialAccountRepository;
import com.example.user.domain.SocialAccount;
import com.example.user.domain.SocialProvider;
import com.example.user.domain.User;

@Repository
class SocialAccountPersistenceAdapter implements SocialAccountRepository {

    private final SpringDataSocialAccountRepository socialAccountRepository;
    private final SpringDataUserRepository userRepository;

    SocialAccountPersistenceAdapter(
            SpringDataSocialAccountRepository socialAccountRepository, SpringDataUserRepository userRepository) {
        this.socialAccountRepository = socialAccountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findUser(SocialProvider provider, String providerUserId) {
        return socialAccountRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .flatMap(account -> userRepository.findById(account.userId()))
                .map(UserJpaEntity::toDomain);
    }

    @Override
    public void save(SocialAccount socialAccount) {
        socialAccountRepository.save(SocialAccountJpaEntity.from(socialAccount));
    }
}
