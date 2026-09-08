package com.example.activity.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.activity.application.port.in.DescribeActivityUseCase;
import com.example.activity.application.port.in.RecordUserRegistrationActivityUseCase;
import com.example.shared.error.BusinessException;
import com.example.user.UserLookup;
import com.example.user.UserRegistered;
import com.example.user.UserSummary;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ActivityService implements DescribeActivityUseCase, RecordUserRegistrationActivityUseCase {

    private final UserLookup userLookup;

    public ActivityService(UserLookup userLookup) {
        this.userLookup = userLookup;
    }

    @Override
    @Transactional(readOnly = true)
    public String describeFor(Long userId) {
        UserSummary user =
                userLookup.findById(userId).orElseThrow(() -> new BusinessException(ActivityErrorCode.USER_NOT_FOUND));
        return "Activity for " + user.displayName();
    }

    @Override
    public void record(UserRegistered event) {
        log.info("User registration activity received: userId={}, occurredAt={}", event.userId(), event.occurredAt());
    }
}
