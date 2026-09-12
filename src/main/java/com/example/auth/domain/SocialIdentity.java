package com.example.auth.domain;

public record SocialIdentity(String provider, String subject, String displayName) {
    public SocialIdentity {
        if (provider == null
                || provider.isBlank()
                || subject == null
                || subject.isBlank()
                || displayName == null
                || displayName.isBlank()
                || displayName.length() > 100) {
            throw new IllegalArgumentException("invalid social identity");
        }
    }
}
