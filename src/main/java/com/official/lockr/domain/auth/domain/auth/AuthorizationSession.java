package com.official.lockr.domain.auth.domain.auth;

import java.time.ZonedDateTime;
import java.util.Objects;

public class AuthorizationSession {

    private final Long id;
    private final String userId;
    private final String deviceId;
    private final String deviceInfo;
    private final String ipAddress;
    private final String userAgent;
    private final ZonedDateTime expiredAt;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;
    private final ZonedDateTime deletedAt;

    public AuthorizationSession(final Long id, final String userId, final String deviceId,
                                final String deviceInfo, final String ipAddress, final String userAgent,
                                final ZonedDateTime expiredAt, final ZonedDateTime createdAt, final ZonedDateTime updatedAt, final ZonedDateTime deletedAt) {
        this.id = id;
        this.userId = userId;
        this.deviceId = deviceId;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final AuthorizationSession that = (AuthorizationSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
