package com.official.lockr.domain.auth.application.oidc;

import com.official.lockr.domain.auth.application.oidc.command.RetrieveOidcProviderIdCommand;

public interface RetrieveOidcProviderIdUseCase {

    default String retrieve(final String idToken, final String providerType) {
        return this.retrieve(new RetrieveOidcProviderIdCommand(idToken, providerType));
    }

    String retrieve(final RetrieveOidcProviderIdCommand command);
}
