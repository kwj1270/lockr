package com.official.lockr.domain.auth.api.dto;

public record AdminLoginHttpRequest(
        String providerId,
        String providerType
) {
}
