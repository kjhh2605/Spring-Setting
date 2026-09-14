package com.example.auth.application.port.out;

import com.example.auth.domain.LoginUser;

public interface ProvisionLoginUserPort {

    LoginUser provision(String providerUserId, String displayName);
}
