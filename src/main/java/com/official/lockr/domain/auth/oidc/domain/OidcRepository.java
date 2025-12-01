package com.official.lockr.domain.auth.oidc.domain;

public interface OidcRepository {
    Oidc save(final Oidc oidc);

    Oidc find(final String providerId, final String provider);
}
