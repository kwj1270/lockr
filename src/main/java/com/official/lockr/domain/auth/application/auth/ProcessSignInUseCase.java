package com.official.lockr.domain.auth.application.auth;

import com.official.lockr.domain.auth.application.auth.command.ProcessSignInCommand;
import com.official.lockr.domain.auth.domain.auth.SignIn;

public interface ProcessSignInUseCase {

    default SignIn process(final String providerId, final String providerType, final String deviceId,
                           final String deviceInfo, final String ipAddress, final String userAgent) {
        return this.process(
                new ProcessSignInCommand(providerId, providerType,  deviceId, deviceInfo, ipAddress, userAgent)
        );
    }

    SignIn process(final ProcessSignInCommand command);

}
