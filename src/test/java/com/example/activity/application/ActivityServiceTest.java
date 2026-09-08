package com.example.activity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.shared.error.BusinessException;
import com.example.user.UserLookup;
import com.example.user.UserSummary;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private UserLookup userLookup;

    @Test
    void describesActivityForExistingUser() {
        when(userLookup.findById(1L)).thenReturn(Optional.of(new UserSummary(1L, "홍길동")));

        ActivityService service = new ActivityService(userLookup);

        assertThat(service.describeFor(1L)).isEqualTo("Activity for 홍길동");
    }

    @Test
    void failsWhenUserDoesNotExist() {
        when(userLookup.findById(404L)).thenReturn(Optional.empty());

        ActivityService service = new ActivityService(userLookup);

        assertThatThrownBy(() -> service.describeFor(404L))
                .isInstanceOfSatisfying(BusinessException.class, exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ActivityErrorCode.USER_NOT_FOUND));
    }
}
