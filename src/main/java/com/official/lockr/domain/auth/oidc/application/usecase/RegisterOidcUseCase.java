package com.official.lockr.domain.auth.oidc.application.usecase;

import com.official.lockr.domain.auth.oidc.application.command.RegisterOidcCommand;
import com.official.lockr.domain.auth.oidc.domain.Oidc;

public interface RegisterOidcUseCase {

    default Oidc register(final String idToken, final String providerType) {
        return this.register(new RegisterOidcCommand(idToken, providerType));
    }

    Oidc register(final RegisterOidcCommand command);
}
