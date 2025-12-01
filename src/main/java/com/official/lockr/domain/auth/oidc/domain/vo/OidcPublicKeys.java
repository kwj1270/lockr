package com.official.lockr.domain.auth.oidc.domain.vo;

import java.util.List;

public record OidcPublicKeys(
        List<OidcPublicKey> keys
) {
    public OidcPublicKey findByOidcPublicKeyId(final OidcPublicKeyId oidcPublicKeyId) {
        return keys.stream()
                .filter(it -> it.match(oidcPublicKeyId))
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }
}
