package com.official.lockr.domain.auth.signin.domain;

import com.official.lockr.global.http.HttpHeaderContext;

import java.io.Serializable;
import java.time.LocalDateTime;

public record SignInSession(
        String userId,
        String deviceId, String deviceName, String deviceOS,
        String ipAddress, String userAgent,
        LocalDateTime createdAt
) implements Serializable {

    public static final String SESSION_KEY = "signIn";

    public static SignInSession from(final SignIn signIn) {
        return new SignInSession(
                signIn.getUserId(),
                signIn.getDeviceId(),
                signIn.getDeviceName(),
                signIn.getDeviceOS(),
                signIn.getIpAddress(),
                signIn.getUserAgent(),
                signIn.getCreatedAt()
        );
    }

    public static SignInSession from(final SignInToken token, final HttpHeaderContext ctx) {
        return new SignInSession(
                token.getUserId(),
                ctx.deviceId(),
                ctx.deviceName(),
                ctx.deviceOS(),
                ctx.ipAddress(),
                ctx.userAgent(),
                token.getCreatedAt()
        );
    }
}
