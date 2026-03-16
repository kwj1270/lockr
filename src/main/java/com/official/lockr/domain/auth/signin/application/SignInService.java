package com.official.lockr.domain.auth.signin.application;

import com.official.lockr.domain.auth.signin.application.command.RegisterSignInCommand;
import com.official.lockr.domain.auth.signin.application.usecase.RegisterSignInUseCase;
import com.official.lockr.domain.auth.signin.domain.SignIn;
import com.official.lockr.domain.auth.signin.domain.SignInRepository;
import org.springframework.stereotype.Service;

@Service
public class SignInService implements RegisterSignInUseCase {

    private final SignInRepository signInRepository;

    public SignInService(final SignInRepository signInRepository) {
        this.signInRepository = signInRepository;
    }

    @Override
    public SignIn register(final RegisterSignInCommand command) {
        final SignIn signIn = SignIn.init(
                command.userId(), command.deviceId(), command.deviceName(),
                command.deviceOS(), command.ipAddress(), command.userAgent()
        );
        return signInRepository.save(signIn);
    }
}
