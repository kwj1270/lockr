package com.official.lockr.domain.notification.application.usecase;

import com.official.lockr.domain.notification.application.command.DeleteNotificationCommand;

public interface DeleteNotificationUseCase {
    void delete(DeleteNotificationCommand command);
}
