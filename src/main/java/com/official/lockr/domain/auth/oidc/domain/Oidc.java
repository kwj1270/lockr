package com.official.lockr.domain.auth.oidc.domain;

import com.official.lockr.domain.auth.oidc.domain.vo.ProviderType;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

public class Oidc {

    private final String id;
    private String userId;
    private final ProviderType provider;
    private final String identifier;
    private final String metadata;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Oidc init(final String provider, final String identifier) {
        final LocalDateTime now = LocalDateTime.now();
        final ProviderType providerType = ProviderType.valueOf(provider);
        return new Oidc(generateUlid(), null, providerType, identifier, null, now, now, null);
    }

    public Oidc(final String id,
                final String userId,
                final ProviderType provider,
                final String identifier,
                final String metadata,
                final LocalDateTime createdAt,
                final LocalDateTime updatedAt,
                final LocalDateTime deletedAt) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.identifier = identifier;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public boolean hasNotUserId() {
        return isNull(userId);
    }

    public void setUserId(final String userId) {
        this.userId = userId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public ProviderType getProvider() {
        return provider;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getMetadata() {
        return metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Oidc oidc = (Oidc) o;
        return Objects.equals(id, oidc.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
