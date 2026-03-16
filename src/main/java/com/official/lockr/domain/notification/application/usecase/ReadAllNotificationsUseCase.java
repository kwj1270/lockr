package com.official.lockr.domain.notification.application.usecase;

import com.official.lockr.domain.notification.application.command.ReadAllNotificationsCommand;

public interface ReadAllNotificationsUseCase {
    void readAll(ReadAllNotificationsCommand command);
}