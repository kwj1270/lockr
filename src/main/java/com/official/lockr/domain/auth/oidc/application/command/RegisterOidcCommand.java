package com.official.lockr.domain.auth.oidc.application.command;

public record RegisterOidcCommand(
        String idToken,
        String provider
) {
}
