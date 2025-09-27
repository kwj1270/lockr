package com.official.lockr.domain.auth.domain.auth;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

public record SignInSession(
        String id, String userId, String providerId, ProviderType providerType, String deviceId,
        String deviceInfo, String ipAddress, String userAgent, ZonedDateTime createdAt
) implements Serializable {

    public SignInSession(final SignIn signIn) {
        this(
                signIn.getId(),
                signIn.getUserId(),
                signIn.getProviderId(),
                signIn.getProviderType(),
                signIn.getDeviceId(),
                signIn.getDeviceInfo(),
                signIn.getIpAddress(),
                signIn.getUserAgent(),
                signIn.getCreatedAt()
        );
    }
}
