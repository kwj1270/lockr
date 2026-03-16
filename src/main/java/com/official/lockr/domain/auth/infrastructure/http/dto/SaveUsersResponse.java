package com.official.lockr.domain.auth.infrastructure.http.dto;

public record SaveUsersResponse(
        String id,
        String providerId,
        String providerType
) {
}
