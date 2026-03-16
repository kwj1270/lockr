package com.official.lockr.domain.auth.domain.oidc;

public record OidcPublicKey(
        String kid,
        String alg,
        String kty,
        String use,
        String n,
        String e
) {

    public boolean match(final OidcPublicKeyId oidcPublicKeyId) {
        return oidcPublicKeyId.match(kid, alg);
    }
}
