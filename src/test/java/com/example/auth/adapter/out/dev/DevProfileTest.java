package com.example.auth.adapter.out.dev;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.example.auth.adapter.in.web.DevTokenController;
import com.example.auth.application.port.in.command.SessionUseCase;
import com.example.auth.application.service.DevTokenService;

class DevProfileTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(DevCredentialAdapter.class, DevTokenService.class, DevTokenController.class)
            .withBean(SessionUseCase.class, () -> mock(SessionUseCase.class));

    @Test
    void enablesOnlyDevAndRequiresSecret() {
        runner.withPropertyValues(
                        "spring.profiles.active=dev", "app.auth.dev.master-secret=development-only-test-secret-32bytes")
                .run(context -> {
                    assertThat(context).hasSingleBean(DevTokenController.class);
                    assertThat(context.getBean(DevCredentialAdapter.class)
                                    .matches("development-only-test-secret-32bytes"))
                            .isTrue();
                    assertThat(context.getBean(DevCredentialAdapter.class).matches("wrong"))
                            .isFalse();
                });
        runner.withPropertyValues("spring.profiles.active=dev")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void disablesForDefaultLocalAndEitherOrderOfMixedProdProfiles() {
        for (String profiles : new String[] {"", "local", "prod", "dev,prod", "prod,dev"}) {
            runner.withPropertyValues("spring.profiles.active=" + profiles).run(context -> {
                assertThat(context).doesNotHaveBean(DevTokenController.class);
                assertThat(context).doesNotHaveBean(DevTokenService.class);
                assertThat(context).doesNotHaveBean(DevCredentialAdapter.class);
            });
        }
    }
}
