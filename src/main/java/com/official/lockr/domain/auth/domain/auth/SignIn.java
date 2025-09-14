package com.official.lockr.domain.auth.domain.auth;

import java.time.ZonedDateTime;
import java.util.Objects;

public class SignIn {

    private final String id;
    private final String userId;
    private final String providerId;
    private final ProviderType providerType;
    private final String deviceId;
    private final String deviceInfo;
    private final String ipAddress;
    private final String userAgent;
    private final ZonedDateTime createdAt;

    public SignIn(final String id, final String userId, final String providerId, final ProviderType providerType, final String deviceId, final String deviceInfo, final String ipAddress, final String userAgent, final ZonedDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.providerId = providerId;
        this.providerType = providerType;
        this.deviceId = deviceId;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getProviderId() {
        return providerId;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public String getProviderTypeValue() {
        return providerType.name();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
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
