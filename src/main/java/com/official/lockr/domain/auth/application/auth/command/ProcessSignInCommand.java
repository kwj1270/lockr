package com.official.lockr.domain.auth.application.auth.command;

public record ProcessSignInCommand(
        String providerId,
        String providerType,
        String deviceId,
        String deviceInfo,
        String ipAddress,
        String userAgent
) {
}
