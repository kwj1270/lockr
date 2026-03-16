package com.official.lockr.domain.club.feed.domain.entity;

import java.time.LocalDateTime;

public class Heart {

    private final String id;
    private final String feedId;
    private final String userId;
    private final LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public Heart(final String id, final String feedId, final String userId, final LocalDateTime createdAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.feedId = feedId;
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

    public boolean isSameUser(final String userId) {
        return this.userId.equals(userId);
    }

    public String getId() {
        return id;
    }

    public String getFeedId() {
        return feedId;
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
