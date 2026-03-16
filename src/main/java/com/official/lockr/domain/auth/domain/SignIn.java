package com.official.lockr.domain.auth.domain;

import java.time.ZonedDateTime;
import java.util.Objects;

public class SignIn {

    private final Long id;
    private final String userId;
    private final AuthorizationType authorizationType;
    private final String deviceId;
    private final String deviceInfo;
    private final String ipAddress;
    private final String userAgent;
    private final ZonedDateTime createdAt;

    public SignIn(final Long id, final String userId, final AuthorizationType authorizationType, final String deviceId, final String deviceInfo, final String ipAddress, final String userAgent, final ZonedDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.authorizationType = authorizationType;
        this.deviceId = deviceId;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final SignIn that = (SignIn) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
