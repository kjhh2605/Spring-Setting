package com.example.user.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.example.user.domain.User;
import com.example.user.domain.UserId;

@Entity
@Table(
        name = "app_user",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_app_user_social",
                        columnNames = {"social_provider", "social_subject"}))
class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "social_provider", length = 32)
    private String socialProvider;

    @Column(name = "social_subject", length = 128)
    private String socialSubject;

    protected UserJpaEntity() {}

    private UserJpaEntity(String displayName) {
        this.displayName = displayName;
    }

    static UserJpaEntity from(User user) {
        return new UserJpaEntity(user.displayName());
    }

    User toDomain() {
        return User.reconstitute(new UserId(id), displayName);
    }
}
