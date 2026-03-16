package com.official.lockr.domain.auth.signin.application.command;

public record RegisterSignInCommand(
        String userId,
        String deviceId,
        String deviceName,
        String deviceOS,
        String ipAddress,
        String userAgent
) {
}
