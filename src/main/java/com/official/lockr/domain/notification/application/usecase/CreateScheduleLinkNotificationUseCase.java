package com.official.lockr.domain.notification.application.usecase;

import com.official.lockr.domain.notification.application.command.CreateScheduleLinkNotificationCommand;

public interface CreateScheduleLinkNotificationUseCase {
    void create(CreateScheduleLinkNotificationCommand command);
}
