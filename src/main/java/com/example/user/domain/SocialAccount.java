package com.example.user.domain;

public record SocialAccount(String provider, String subject, String displayName) {
    public SocialAccount {
        if (provider == null
                || provider.isBlank()
                || provider.length() > 32
                || subject == null
                || subject.isBlank()
                || subject.length() > 128) {
            throw new IllegalArgumentException("invalid social account");
        }
        User.register(displayName);
    }
}
