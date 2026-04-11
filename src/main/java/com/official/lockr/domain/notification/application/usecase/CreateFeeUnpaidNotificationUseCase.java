package com.official.lockr.domain.notification.application.usecase;

import com.official.lockr.domain.notification.application.command.CreateFeeUnpaidNotificationCommand;

public interface CreateFeeUnpaidNotificationUseCase {
    void create(CreateFeeUnpaidNotificationCommand command);
}
