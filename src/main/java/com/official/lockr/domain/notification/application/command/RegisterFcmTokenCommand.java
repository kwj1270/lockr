package com.official.lockr.domain.notification.application.command;

public record RegisterFcmTokenCommand(
        String userId,
        String token,
        String deviceId
) {
}
