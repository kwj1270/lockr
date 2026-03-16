package com.official.lockr.domain.users.api.dto;

public record FindUsersByProviderRequest(
        String providerId,
        String providerType
) {
}
