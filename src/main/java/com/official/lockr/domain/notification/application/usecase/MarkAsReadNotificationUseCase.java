package com.official.lockr.domain.notification.application.usecase;

import com.official.lockr.domain.notification.application.command.MarkAsReadNotificationCommand;
import com.official.lockr.domain.notification.domain.Notification;

public interface MarkAsReadNotificationUseCase {
    Notification markAsRead(MarkAsReadNotificationCommand command);
}
