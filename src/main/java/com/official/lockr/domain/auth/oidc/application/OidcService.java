package com.official.lockr.domain.auth.oidc.application;

import com.official.lockr.domain.auth.signup.domain.SignUp;
import com.official.lockr.domain.auth.signup.domain.SignUpRepository;
import com.official.lockr.domain.auth.oidc.application.command.RegisterOidcCommand;
import com.official.lockr.domain.auth.oidc.domain.Oidc;
import com.official.lockr.domain.auth.oidc.domain.OidcProviders;
import com.official.lockr.domain.auth.oidc.domain.OidcRepository;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class OidcService implements RegisterOidcUseCase {

    private final OidcProviders oidcProviders;
    private final OidcRepository oidcRepository;
    private final SignUpRepository signUpRepository;

    public OidcService(final OidcProviders oidcProviders,
                       final OidcRepository oidcRepository,
                       SignUpRepository signUpRepository
    ) {
        this.oidcProviders = oidcProviders;
        this.oidcRepository = oidcRepository;
        this.signUpRepository = signUpRepository;
    }

    @Override
    public Oidc register(final RegisterOidcCommand command) {
        final String identifier = oidcProviders.identifier(command.idToken(), command.provider());
        final Oidc oidc = oidc(command, identifier);
        if (oidc.hasNotUserId()) {
            return updateUserId(oidc);
        }
        return oidc;
    }

    private Oidc updateUserId(final Oidc oidc) {
        final SignUp signUp = signUpRepository.save();
        final String userId = signUp.getUserId();
        if(isNull(userId) || userId.isBlank()) {
            throw new IllegalArgumentException();
        }
        oidc.setUserId(userId);
        try {
            return oidcRepository.save(oidc);
        } catch (Exception e) {
            signUpRepository.delete(userId);
            throw e;
        }
    }

    private Oidc oidc(final RegisterOidcCommand command, final String identifier) {
        final Oidc oidc = oidcRepository.find(identifier, command.provider());
        if (nonNull(oidc)) {
            return oidc;
        }
        return oidcRepository.save(Oidc.init(command.provider(), identifier));
    }
}
