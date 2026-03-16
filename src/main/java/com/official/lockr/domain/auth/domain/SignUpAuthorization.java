package com.official.lockr.domain.auth.domain;

import java.time.ZonedDateTime;
import java.util.Objects;

public class SignUpAuthorization {

    private final String id;
    private final String userId;
    private final String authorizationId;
    private final AuthorizationType authorizationType;
    private final Object authorizationAdditionalInfo;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;
    private final ZonedDateTime deletedAt;

    public SignUpAuthorization(final String id, final String userId, final String authorizationId, final AuthorizationType authorizationType, final Object authorizationAdditionalInfo, final ZonedDateTime createdAt, final ZonedDateTime updatedAt, final ZonedDateTime deletedAt) {
        this.id = id;
        this.userId = userId;
        this.authorizationId = authorizationId;
        this.authorizationType = authorizationType;
        this.authorizationAdditionalInfo = authorizationAdditionalInfo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
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
