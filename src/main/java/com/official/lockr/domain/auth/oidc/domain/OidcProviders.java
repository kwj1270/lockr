package com.official.lockr.domain.auth.oidc.domain;

public interface OidcProviders {
    String identifier(final String idToken, final String providerType);
}
