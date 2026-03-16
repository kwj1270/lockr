package com.official.lockr.domain.users.api.dto;

public class FindUsersByProviderResponse {
    String id;
    String providerId;
    String providerType;

    public FindUsersByProviderResponse(final String id, final String providerId, final String providerType) {
        this.id = id;
        this.providerId = providerId;
        this.providerType = providerType;
    }

    public FindUsersByProviderResponse() {
    }

    public String getId() {
        return id;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getProviderType() {
        return providerType;
    }
}
