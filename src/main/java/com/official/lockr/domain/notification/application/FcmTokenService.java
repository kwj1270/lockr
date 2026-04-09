package com.official.lockr.domain.notification.application;

import com.official.lockr.domain.notification.application.command.DeleteFcmTokenCommand;
import com.official.lockr.domain.notification.application.command.RegisterFcmTokenCommand;
import com.official.lockr.domain.notification.application.usecase.DeleteFcmTokenUseCase;
import com.official.lockr.domain.notification.application.usecase.RegisterFcmTokenUseCase;
import com.official.lockr.domain.notification.domain.FcmToken;
import com.official.lockr.domain.notification.domain.FcmTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class FcmTokenService implements RegisterFcmTokenUseCase, DeleteFcmTokenUseCase {

    private final FcmTokenRepository fcmTokenRepository;

    public FcmTokenService(final FcmTokenRepository fcmTokenRepository) {
        this.fcmTokenRepository = fcmTokenRepository;
    }

    @Override
    public FcmToken register(final RegisterFcmTokenCommand command) {
        final FcmToken existing = fcmTokenRepository.findByUserIdAndDeviceId(command.userId(), command.deviceId());
        if (existing != null) {
            existing.updateToken(command.token());
            return fcmTokenRepository.save(existing);
        }
        final FcmToken fcmToken = FcmToken.init(command.userId(), command.token(), command.deviceId());
        return fcmTokenRepository.save(fcmToken);
    }

    @Override
    public void delete(final DeleteFcmTokenCommand command) {
        fcmTokenRepository.deleteByToken(command.token());
    }
}
