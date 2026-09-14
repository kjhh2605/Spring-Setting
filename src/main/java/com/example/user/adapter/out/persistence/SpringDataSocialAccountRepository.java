package com.example.user.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user.domain.SocialProvider;

interface SpringDataSocialAccountRepository extends JpaRepository<SocialAccountJpaEntity, Long> {

    Optional<SocialAccountJpaEntity> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
}
