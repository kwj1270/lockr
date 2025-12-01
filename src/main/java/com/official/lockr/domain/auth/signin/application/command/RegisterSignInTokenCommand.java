package com.official.lockr.domain.auth.signin.application.command;

public record RegisterSignInTokenCommand(
        String signInId,
        String userId
) {
}
