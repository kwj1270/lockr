package com.official.lockr.domain.auth.signin.domain;

import com.official.lockr.domain.auth.signin.domain.event.ProcessedSignInEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

public class SignIn extends AggregateRoot {

    private final String id;
    private final String userId;
    private final String deviceId;
    private final String deviceName;
    private final String deviceOS;
    private final String ipAddress;
    private final String userAgent;
    private final LocalDateTime createdAt;

    public static SignIn init(final String userId, final String deviceId, final String deviceName, final String deviceOS, final String ipAddress, final String userAgent) {
        final SignIn signIn = new SignIn(generateUlid(), userId, deviceId, deviceName, deviceOS, ipAddress, userAgent, LocalDateTime.now());
        signIn.addEvent(new ProcessedSignInEvent(signIn.id, signIn.userId, signIn.deviceId, signIn.deviceName, signIn.deviceOS, signIn.ipAddress, signIn.userAgent, signIn.createdAt));
        return signIn;
    }

    public SignIn(final String id, final String userId, final String deviceId, final String deviceName, final String deviceOS, final String ipAddress, final String userAgent, final LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceOS = deviceOS;
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

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LocalDateTime getCreatedAt() {
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
