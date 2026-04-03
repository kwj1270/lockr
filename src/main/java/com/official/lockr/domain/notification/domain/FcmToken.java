package com.official.lockr.domain.notification.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

public class FcmToken {
    private final String id;
    private final String userId;
    private String token;
    private final String deviceId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private FcmToken(
            final String id,
            final String userId,
            final String token,
            final String deviceId,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.deviceId = deviceId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FcmToken init(
            final String userId,
            final String token,
            final String deviceId
    ) {
        final LocalDateTime now = LocalDateTime.now();
        return new FcmToken(generateUlid(), userId, token, deviceId, now, now);
    }

    public static FcmToken from(
            final String id,
            final String userId,
            final String token,
            final String deviceId,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt
    ) {
        return new FcmToken(id, userId, token, deviceId, createdAt, updatedAt);
    }

    public void updateToken(final String newToken) {
        this.token = newToken;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getToken() { return token; }
    public String getDeviceId() { return deviceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final FcmToken that = (FcmToken) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
