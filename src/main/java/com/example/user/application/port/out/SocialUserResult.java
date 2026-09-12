package com.example.user.application.port.out;

import com.example.user.domain.User;

public record SocialUserResult(User user, boolean created) {}
