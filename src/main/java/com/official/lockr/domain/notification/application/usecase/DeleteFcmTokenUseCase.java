package com.official.lockr.domain.notification.application.usecase;

import com.official.lockr.domain.notification.application.command.DeleteFcmTokenCommand;

public interface DeleteFcmTokenUseCase {
    void delete(DeleteFcmTokenCommand command);
}
