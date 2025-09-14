package com.official.lockr.domain.auth.application.oidc;

import com.official.lockr.domain.auth.application.oidc.command.RetrieveOidcProviderIdCommand;
import com.official.lockr.domain.auth.domain.oidc.OidcProviders;
import org.springframework.stereotype.Service;

@Service
public class OidcProviderIdService implements RetrieveOidcProviderIdUseCase {

    private final OidcProviders oidcProviders;

    public OidcProviderIdService(final OidcProviders oidcProviders) {
        this.oidcProviders = oidcProviders;
    }

    @Override
    public String retrieve(final RetrieveOidcProviderIdCommand command) {
        return oidcProviders.providerId(command.idToken(), command.provider());
    }
}
