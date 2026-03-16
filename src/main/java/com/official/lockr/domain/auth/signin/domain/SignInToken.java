package com.official.lockr.domain.auth.signin.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static com.official.lockr.global.util.UuidUtils.generateUuid;

public class SignInToken {
    private final String id;
    private final String userId;
    private final String signInId;
    private final String token;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public SignInToken(final String id, final String userId, final String signInId, final String token, final LocalDateTime expiresAt, final LocalDateTime createdAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.userId = userId;
        this.signInId = signInId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public static SignInToken init(final String userId, final String signInId) {
        final LocalDateTime now = LocalDateTime.now();
        return new SignInToken(generateUlid(), userId, signInId, generateUuid(), now.plusDays(30), now, null);
    }

    public void revoke() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return deletedAt != null;
    }

    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getSignInId() {
        return signInId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final SignInToken that = (SignInToken) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
