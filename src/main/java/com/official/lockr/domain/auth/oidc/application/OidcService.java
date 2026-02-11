package com.official.lockr.domain.auth.oidc.application;

import com.official.lockr.domain.auth.oidc.application.usecase.RegisterOidcUseCase;
import com.official.lockr.domain.auth.oidc.application.command.RegisterOidcCommand;
import com.official.lockr.domain.auth.oidc.domain.Oidc;
import com.official.lockr.domain.auth.oidc.domain.OidcProviders;
import com.official.lockr.domain.auth.oidc.domain.OidcRepository;
import com.official.lockr.domain.users.application.RegisterUsersUseCase;
import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.domain.Users;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Service
public class OidcService implements RegisterOidcUseCase {

    private final OidcProviders oidcProviders;
    private final OidcRepository oidcRepository;
    private final RegisterUsersUseCase registerUsersUseCase;

    public OidcService(final OidcProviders oidcProviders,
                       final OidcRepository oidcRepository,
                       final RegisterUsersUseCase registerUsersUseCase
    ) {
        this.oidcProviders = oidcProviders;
        this.oidcRepository = oidcRepository;
        this.registerUsersUseCase = registerUsersUseCase;
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
        final Users users = registerUsersUseCase.register(new SaveUsersCommand());
        oidc.setUserId(users.getId());
        return oidcRepository.save(oidc);
    }

    private Oidc oidc(final RegisterOidcCommand command, final String identifier) {
        final Oidc oidc = oidcRepository.find(identifier, command.provider());
        if (nonNull(oidc)) {
            return oidc;
        }
        return oidcRepository.save(Oidc.init(command.provider(), identifier));
    }
}
