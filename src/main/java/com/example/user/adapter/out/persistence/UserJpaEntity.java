package com.example.user.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.example.user.domain.User;
import com.example.user.domain.UserId;
import com.example.user.domain.UserStatus;

@Entity
@Table(name = "app_user")
class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private UserStatus status;

    protected UserJpaEntity() {}

    private UserJpaEntity(String displayName, UserStatus status) {
        this.displayName = displayName;
        this.status = status;
    }

    static UserJpaEntity from(User user) {
        return new UserJpaEntity(user.displayName(), user.status());
    }

    User toDomain() {
        return User.reconstitute(new UserId(id), displayName, status);
    }
}
