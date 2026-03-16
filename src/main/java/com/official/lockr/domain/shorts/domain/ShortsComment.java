package com.official.lockr.domain.shorts.domain;

import java.time.LocalDateTime;

public class ShortsComment {

    private final String id;
    private final String shortsId;
    private final String userId;
    private final String content;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public ShortsComment(final String id, final String shortsId, final String userId, final String content,
                         final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.shortsId = shortsId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static ShortsComment create(final String id, final String shortsId, final String userId, final String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다");
        }
        return new ShortsComment(id, shortsId, userId, content, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public void delete(final String userId) {
        if (!canEditBy(userId)) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다");
        }
        this.deletedAt = LocalDateTime.now();
    }

    public boolean canEditBy(final String userId) {
        return this.userId.equals(userId);
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

    public String getContent() {
        return content;
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
}
