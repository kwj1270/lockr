package com.official.lockr.domain.auth.application.auth;

import com.github.f4b6a3.ulid.UlidCreator;
import com.official.lockr.domain.auth.application.auth.command.ProcessSignInCommand;
import com.official.lockr.domain.auth.domain.auth.SignIn;
import com.official.lockr.domain.auth.domain.auth.SignInRepository;
import com.official.lockr.domain.auth.domain.auth.SignUpAuthorization;
import com.official.lockr.domain.auth.domain.auth.SignUpAuthorizationRepository;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Objects;

@Service
public class SignInService implements ProcessSignInUseCase {

    private final SignUpAuthorizationRepository signUpAuthorizationRepository;
    private final SignInRepository signInRepository;

    public SignInService(final SignUpAuthorizationRepository signUpAuthorizationRepository,
                         final SignInRepository signInRepository) {
        this.signUpAuthorizationRepository = signUpAuthorizationRepository;
        this.signInRepository = signInRepository;
    }

    @Override
    public SignIn process(final ProcessSignInCommand command) {
        final SignUpAuthorization signUpAuthorization = signUpAuthorization(command.providerId(), command.providerType());
        final SignIn signIn = mapToSignIn(signUpAuthorization, command);
        return signInRepository.save(signIn);
    }

    private SignIn mapToSignIn(final SignUpAuthorization signUpAuthorization, final ProcessSignInCommand command) {
        return new SignIn(
                UlidCreator.getUlid().toString(),
                signUpAuthorization.getId(),
                signUpAuthorization.getProviderId(),
                signUpAuthorization.getProviderType(),
                command.deviceId(),
                command.deviceInfo(),
                command.ipAddress(),
                command.userAgent(),
                ZonedDateTime.now()
        );
    }

    private SignUpAuthorization signUpAuthorization(final String providerId, final String providerType) {
        final SignUpAuthorization signUpAuthorization = signUpAuthorizationRepository.find(providerId, providerType);
        if (Objects.nonNull(signUpAuthorization)) {
            return signUpAuthorization;
        }
        return signUpAuthorizationRepository.save(providerId, providerType);
    }
}
