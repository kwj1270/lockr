package com.official.lockr.domain.shorts.domain;

import java.time.LocalDateTime;

public class ShortsHeart {

    private final String id;
    private final String shortsId;
    private final String userId;
    private final LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public ShortsHeart(final String id, final String shortsId, final String userId,
                       final LocalDateTime createdAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.shortsId = shortsId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public String getId() {
        return id;
    }

    public String getShortsId() {
        return shortsId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
