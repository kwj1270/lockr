package com.official.lockr.domain.auth.infrastructure.http.dto;

public record FindUsersByProviderResponse(
        String id,
        String providerId,
        String providerType
) {
}
