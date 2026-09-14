package com.example.auth.adapter.out.user;

import org.springframework.stereotype.Component;

import com.example.auth.application.port.out.ProvisionLoginUserPort;
import com.example.auth.domain.LoginUser;
import com.example.user.ProvisionedUser;
import com.example.user.SocialAccountProvisioning;

@Component
class LoginUserProvisioningAdapter implements ProvisionLoginUserPort {

    private final SocialAccountProvisioning provisioning;

    LoginUserProvisioningAdapter(SocialAccountProvisioning provisioning) {
        this.provisioning = provisioning;
    }

    @Override
    public LoginUser provision(String providerUserId, String displayName) {
        ProvisionedUser user = provisioning.provisionKakaoUser(providerUserId, displayName);
        return new LoginUser(user.id(), user.onboardingRequired());
    }
}
