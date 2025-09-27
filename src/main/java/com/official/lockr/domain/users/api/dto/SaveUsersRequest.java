package com.official.lockr.domain.users.api.dto;

import com.official.lockr.domain.users.application.command.SaveUsersCommand;

public record SaveUsersRequest(
        String providerId,
        String providerType
) {
    public SaveUsersCommand command() {
        return new SaveUsersCommand(providerId, providerType);
    }
}
