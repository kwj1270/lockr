package com.official.lockr.domain.users.api.dto;

public record SaveUsersResponse(
        String id,
        String providerId,
        String providerType
) {
}
