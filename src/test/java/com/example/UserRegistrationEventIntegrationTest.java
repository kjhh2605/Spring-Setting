package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.activity.application.ActivityService;
import com.example.support.IntegrationTestSupport;
import com.example.user.UserLookup;
import com.example.user.application.port.in.RegisterUserCommand;
import com.example.user.application.port.in.RegisterUserUseCase;
import com.example.user.application.port.in.RegisteredUserInfo;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

class UserRegistrationEventIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private RegisterUserUseCase registerUser;

    @Autowired
    private UserLookup userLookup;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final ActivityLogAppender appender = new ActivityLogAppender();
    private final Logger activityLogger = (Logger) LoggerFactory.getLogger(ActivityService.class);

    @BeforeEach
    void captureActivityLogs() {
        appender.start();
        activityLogger.addAppender(appender);
    }

    @AfterEach
    void stopCapturingActivityLogs() {
        activityLogger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void recordsRegistrationAfterCommitOnAnotherThread() throws InterruptedException {
        long publishingThreadId = Thread.currentThread().threadId();
        RegisteredUserInfo user = new TransactionTemplate(transactionManager).execute(status -> {
            RegisteredUserInfo registered = registerUser.register(new RegisterUserCommand("커밋 사용자"));
            assertThat(appender.events).as("커밋 전에는 활동을 기록하지 않는다").isEmpty();
            return registered;
        });

        ActivityLog activity = appender.events.poll(5, TimeUnit.SECONDS);

        assertThat(user).isNotNull();
        assertThat(activity).as("커밋한 사용자 등록의 후속 활동이 기록된다").isNotNull();
        assertThat(activity.userId()).isEqualTo(user.id());
        assertThat(activity.threadId()).isNotEqualTo(publishingThreadId);
        assertThat(activity.transactionActive()).isTrue();
        assertThat(userLookup.findById(user.id())).isPresent();
    }

    @Test
    void doesNotRecordRolledBackRegistration() throws InterruptedException {
        RegisteredUserInfo user = new TransactionTemplate(transactionManager).execute(status -> {
            RegisteredUserInfo registered = registerUser.register(new RegisterUserCommand("롤백 사용자"));
            status.setRollbackOnly();
            return registered;
        });

        assertThat(user).isNotNull();
        assertThat(userLookup.findById(user.id())).isEmpty();
        assertThat(appender.events.poll(500, TimeUnit.MILLISECONDS))
                .as("롤백한 사용자 등록은 활동을 기록하지 않는다")
                .isNull();
    }

    private record ActivityLog(Long userId, long threadId, boolean transactionActive) {}

    private static class ActivityLogAppender extends AppenderBase<ILoggingEvent> {

        private final BlockingQueue<ActivityLog> events = new LinkedBlockingQueue<>();

        @Override
        protected void append(ILoggingEvent event) {
            if (event.getMessage().startsWith("User registration activity received:")) {
                events.add(new ActivityLog(
                        (Long) event.getArgumentArray()[0],
                        Thread.currentThread().threadId(),
                        TransactionSynchronizationManager.isActualTransactionActive()));
            }
        }
    }
}
