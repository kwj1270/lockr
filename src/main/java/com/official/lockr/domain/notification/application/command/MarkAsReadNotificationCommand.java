package com.official.lockr.domain.notification.application.command;

public record MarkAsReadNotificationCommand(
        String notificationId,
        String userId
) {
}
