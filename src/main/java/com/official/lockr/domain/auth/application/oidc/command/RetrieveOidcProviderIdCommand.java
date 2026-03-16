package com.official.lockr.domain.auth.application.oidc.command;

public record RetrieveOidcProviderIdCommand(
        String idToken,
        String provider
) {
}
