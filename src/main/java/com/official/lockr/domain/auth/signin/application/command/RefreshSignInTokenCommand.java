package com.official.lockr.domain.auth.signin.application.command;

public record RefreshSignInTokenCommand(
        String signInToken
) {
}
