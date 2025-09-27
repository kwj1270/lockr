package com.official.lockr.domain.users.application.command;

public record SaveUsersCommand(
        String providerId,
        String providerType
) {
}
