package com.official.lockr.domain.notification.application.usecase;

import com.official.lockr.domain.notification.application.command.RegisterFcmTokenCommand;
import com.official.lockr.domain.notification.domain.FcmToken;

public interface RegisterFcmTokenUseCase {
    FcmToken register(RegisterFcmTokenCommand command);
}
