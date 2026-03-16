package com.official.lockr.domain.auth.domain.auth;

import java.util.Objects;

public class SignUpAuthorization {

    private final String id;
    private final String providerId;
    private final ProviderType providerType;

    public SignUpAuthorization(final String id, final String providerId, final String providerType) {
        this(id, providerId, ProviderType.valueOf(providerType));
    }

    public SignUpAuthorization(final String id, final String providerId, final ProviderType providerType) {
        this.id = id;
        this.providerId = providerId;
        this.providerType = providerType;
    }


    public String getId() {
        return id;
    }

    public String getProviderId() {
        return providerId;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final SignUpAuthorization that = (SignUpAuthorization) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
