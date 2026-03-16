package com.official.lockr.domain.auth.domain.oidc;

public interface OidcProviders {
    String providerId(final String idToken, final String providerType);
}
