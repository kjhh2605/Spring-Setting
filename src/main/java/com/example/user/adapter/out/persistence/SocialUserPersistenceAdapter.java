package com.example.user.adapter.out.persistence;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.example.user.application.port.out.SocialUserRepository;
import com.example.user.application.port.out.SocialUserResult;
import com.example.user.domain.SocialAccount;
import com.example.user.domain.User;
import com.example.user.domain.UserId;

@Repository
public class SocialUserPersistenceAdapter implements SocialUserRepository {
    private final JdbcClient jdbc;

    public SocialUserPersistenceAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public SocialUserResult findOrCreate(SocialAccount account) {
        var insertedId = jdbc.sql("""
                INSERT INTO app_user (display_name, social_provider, social_subject)
                VALUES (:name, :provider, :subject)
                ON CONFLICT (social_provider, social_subject) DO NOTHING
                RETURNING id
                """)
                .param("name", account.displayName())
                .param("provider", account.provider())
                .param("subject", account.subject())
                .query(Long.class)
                .optional();
        if (insertedId.isPresent()) {
            return new SocialUserResult(User.reconstitute(new UserId(insertedId.get()), account.displayName()), true);
        }
        // 별도 SELECT는 READ COMMITTED에서 동시 최초 등록의 커밋 결과를 읽는다.
        User existing = jdbc.sql("""
                SELECT id, display_name FROM app_user
                WHERE social_provider = :provider AND social_subject = :subject
                """)
                .param("provider", account.provider())
                .param("subject", account.subject())
                .query((rs, row) -> User.reconstitute(new UserId(rs.getLong("id")), rs.getString("display_name")))
                .single();
        return new SocialUserResult(existing, false);
    }
}
