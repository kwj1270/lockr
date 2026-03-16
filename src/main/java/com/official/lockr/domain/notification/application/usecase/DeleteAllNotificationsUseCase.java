package com.official.lockr.domain.notification.application.usecase;

import com.official.lockr.domain.notification.application.command.DeleteAllNotificationsCommand;

public interface DeleteAllNotificationsUseCase {
    void deleteAll(DeleteAllNotificationsCommand command);
}
