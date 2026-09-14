package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;

import com.example.support.IntegrationTestSupport;

@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=validate")
class DatabaseMigrationIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void appliesInitialUserAndSocialAccountSchema() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");
        assertThat(tableExists("app_user")).isTrue();
        assertThat(tableExists("user_social_account")).isTrue();
    }

    private boolean tableExists(String tableName) {
        return jdbcClient
                .sql("SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = :tableName)")
                .param("tableName", tableName)
                .query(Boolean.class)
                .single();
    }
}
