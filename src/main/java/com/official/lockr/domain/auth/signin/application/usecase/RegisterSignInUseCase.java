package com.official.lockr.domain.auth.signin.application.usecase;

import com.official.lockr.domain.auth.signin.application.command.RegisterSignInCommand;
import com.official.lockr.domain.auth.signin.domain.SignIn;

public interface RegisterSignInUseCase {

    default SignIn register(final String userId, final String deviceId, final String deviceName, final String deviceOS,
                            final String ipAddress, final String userAgent) {
        return register(new RegisterSignInCommand(userId, deviceId, deviceName, deviceOS, ipAddress, userAgent));
    }

    SignIn register(final RegisterSignInCommand command);

}
