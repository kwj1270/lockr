package com.official.lockr.domain.club.feed.domain.entity;

import java.time.LocalDateTime;

public class Image {

    private final String id;
    private final String url;
    private final String userId;
    private final LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public Image(final String id, final String url, final String userId, final LocalDateTime createdAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.url = url;
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

    public String getUrl() {
        return url;
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
