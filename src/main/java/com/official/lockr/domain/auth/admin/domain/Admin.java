package com.official.lockr.domain.auth.admin.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import static java.util.Objects.isNull;

public class Admin {
    private final String id;
    private final String password;
    private String userId;
    private final String role;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Admin init(final String id, final String userId, final String password) {
        final LocalDateTime now = LocalDateTime.now();
        return new Admin(id, password, userId, "BASIC", now, now, null);
    }

    public Admin(final String id,
                 final String password,
                 final String userId,
                 final String role,
                 final LocalDateTime createdAt,
                 final LocalDateTime updatedAt,
                 final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.password = password;
        this.userId = userId;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public boolean hasNotUserId() {
        return isNull(userId);
    }

    public boolean matchPassword(final String rawPassword) {
        return this.password.equals(rawPassword);
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
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
        final Admin that = (Admin) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
