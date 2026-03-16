package com.official.lockr.domain.auth.domain.oidc;

public record OidcPublicKeyId(
        String kid,
        String alg
) {
    public boolean match(final String kid, final String alg) {
        return this.kid.equals(kid) &&  this.alg.equals(alg);
    }
}
