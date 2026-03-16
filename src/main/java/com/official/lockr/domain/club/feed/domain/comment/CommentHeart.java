package com.official.lockr.domain.club.feed.domain.comment;

import java.time.LocalDateTime;

public class CommentHeart {

    private final String id;
    private final String commentId;
    private final String userId;
    private final LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public CommentHeart(final String id, final String commentId, final String userId, final LocalDateTime createdAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.commentId = commentId;
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

    public String getCommentId() {
        return commentId;
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
