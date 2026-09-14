package com.example.user.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.example.user.domain.SocialAccount;
import com.example.user.domain.SocialProvider;

@Entity
@Table(
        name = "user_social_account",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_user_social_account_provider_subject",
                        columnNames = {"provider", "provider_user_id"}))
class SocialAccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 32)
    private SocialProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    protected SocialAccountJpaEntity() {}

    private SocialAccountJpaEntity(Long userId, SocialProvider provider, String providerUserId) {
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    static SocialAccountJpaEntity from(SocialAccount socialAccount) {
        return new SocialAccountJpaEntity(
                socialAccount.userId().value(), socialAccount.provider(), socialAccount.providerUserId());
    }

    Long userId() {
        return userId;
    }
}
