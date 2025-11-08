package com.official.lockr.domain.club.feed.domain.comment;

import com.official.lockr.domain.club.feed.domain.entity.Image;
import com.official.lockr.domain.club.feed.domain.entity.Video;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

public class Comment {

    private final String id;
    private final String feedId;
    private final String userId;
    private String content;
    private CommentImages images;
    private CommentVideos videos;
    private CommentHearts hearts;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Comment(final String id, final String feedId, final String userId, final String content,
                   final CommentImages images, final CommentVideos videos, final CommentHearts hearts,
                   final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.feedId = feedId;
        this.userId = userId;
        this.content = content;
        this.images = images;
        this.videos = videos;
        this.hearts = nonNull(hearts) ? hearts : new CommentHearts();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Comment create(final String id, final String feedId, final String userId, final String content,
                                 final CommentImages images, final CommentVideos videos) {
        return new Comment(id, feedId, userId, content, images, videos, new CommentHearts(), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public void update(final String userId, final String content, final CommentImages newImages, final CommentVideos newVideos) {
        if (!canEditBy(userId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다");
        }
        if (isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다");
        }
        // 기존 이미지를 soft delete 처리
        this.images.getImages().forEach(image -> {
            if (!image.isDeleted()) {
                image.delete();
            }
        });

        // 기존 비디오를 soft delete 처리
        this.videos.getVideos().forEach(video -> {
            if (!video.isDeleted()) {
                video.delete();
            }
        });

        // 기존(soft deleted) + 새로운 이미지를 merge
        final List<Image> mergedImages = new ArrayList<>(this.images.getImages());
        mergedImages.addAll(newImages.getImages());
        this.images = new CommentImages(this.id, mergedImages);

        // 기존(soft deleted) + 새로운 비디오를 merge
        final List<Video> mergedVideos = new ArrayList<>(this.videos.getVideos());
        mergedVideos.addAll(newVideos.getVideos());
        this.videos = new CommentVideos(this.id, mergedVideos);

        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete(final String userId) {
        if (!canEditBy(userId)) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다");
        }
        this.deletedAt = LocalDateTime.now();
        this.hearts.delete(userId);
    }

    public CommentHeart addHeart(final String userId, final String heartId) {
        if (isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글에 좋아요를 누를 수 없습니다");
        }
        final boolean alreadyHearted = hearts.isAlready(userId);
        if (alreadyHearted) {
            throw new IllegalStateException("이미 좋아요를 누르셨습니다");
        }
        final CommentHeart heart = new CommentHeart(heartId, this.id, userId, LocalDateTime.now(), null);
        hearts.add(heart);
        return heart;
    }

    public void removeHeart(final String userId) {
        hearts.remove(userId);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean canEditBy(final String userId) {
        return this.userId.equals(userId);
    }

    public List<CommentHeart> getActiveHearts() {
        return hearts.getHearts();
    }

    public int getHeartsCount() {
        return hearts.count();
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

    public String getContent() {
        return content;
    }

    public CommentImages getImages() {
        return images;
    }

    public CommentVideos getVideos() {
        return videos;
    }

    public List<CommentHeart> getHearts() {
        return hearts.getHearts();
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
