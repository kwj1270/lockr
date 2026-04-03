package com.official.lockr.domain.notification.application.command;

public record DeleteFcmTokenCommand(
        String userId,
        String token
) {
}
