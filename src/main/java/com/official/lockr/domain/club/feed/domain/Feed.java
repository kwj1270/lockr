package com.official.lockr.domain.club.feed.domain;

import com.official.lockr.domain.club.feed.domain.comment.Comment;
import com.official.lockr.domain.club.feed.domain.comment.CommentImages;
import com.official.lockr.domain.club.feed.domain.comment.CommentVideos;
import com.official.lockr.domain.club.feed.domain.entity.Heart;
import com.official.lockr.domain.club.feed.domain.entity.Image;
import com.official.lockr.domain.club.feed.domain.entity.Video;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Feed extends AggregateRoot {

    private final String id;
    private final String clubId;
    private final String userId;
    private String title;
    private String content;
    private final FeedType feedType;
    private FeedImages images;
    private FeedVideos videos;
    private List<Comment> comments;
    private List<Heart> hearts;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Feed(final String id, final String clubId, final String title, final String content, final FeedType feedType,
                final FeedImages images, final FeedVideos videos, final List<Comment> comments, final List<Heart> hearts,
                final String userId, final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.clubId = clubId;
        this.title = title;
        this.content = content;
        this.feedType = feedType;
        this.images = images;
        this.videos = videos;
        this.comments = comments != null ? comments : new ArrayList<>();
        this.hearts = hearts != null ? hearts : new ArrayList<>();
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Feed create(final String id, final String clubId, final String userId, final FeedType feedType,
                               final String title, final String content, final FeedImages images, final FeedVideos videos) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("피드 내용은 필수입니다");
        }
        return new Feed(id, clubId, title, content, feedType, images, videos, new ArrayList<>(), new ArrayList<>(),
                userId, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public void update(final String userId, final String title, final String content, final FeedImages newImages, final FeedVideos newVideos) {
        if (!canEditBy(userId)) {
            throw new IllegalStateException("피드 작성자만 수정할 수 있습니다");
        }
        if (isDeleted()) {
            throw new IllegalStateException("삭제된 피드는 수정할 수 없습니다");
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
        this.images = new FeedImages(this.id, mergedImages);

        // 기존(soft deleted) + 새로운 비디오를 merge
        final List<Video> mergedVideos = new ArrayList<>(this.videos.getVideos());
        mergedVideos.addAll(newVideos.getVideos());
        this.videos = new FeedVideos(this.id, mergedVideos);
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete(final String userId, final boolean isStaff) {
        final boolean isAuthor = this.userId.equals(userId);

        if (!isAuthor && !isStaff) {
            throw new IllegalStateException("피드 작성자 또는 운영진만 삭제할 수 있습니다");
        }

        this.deletedAt = LocalDateTime.now();
        this.comments.forEach(comment -> comment.delete(comment.getUserId()));
        this.hearts.forEach(Heart::delete);
    }

    public Comment addComment(final String commentId, final String userId, final String content,
                              final CommentImages images, final CommentVideos videos) {
        if (isDeleted()) {
            throw new IllegalStateException("삭제된 피드에 댓글을 작성할 수 없습니다");
        }
        final Comment comment = Comment.create(commentId, this.id, userId, content, images, videos);
        comments.add(comment);
        return comment;
    }

    public void updateComment(final String commentId, final String userId, final String content,
                              final CommentImages images, final CommentVideos videos) {
        if (isDeleted()) {
            throw new IllegalStateException("삭제된 피드의 댓글은 수정할 수 없습니다");
        }
        final Comment comment = findCommentOrThrow(commentId);
        comment.update(userId, content, images, videos);
    }

    public void addCommentHeart(final String commentId, final String userId, final String heartId) {
        if (isDeleted()) {
            throw new IllegalStateException("삭제된 피드의 댓글에 좋아요를 누를 수 없습니다");
        }
        final Comment comment = findCommentOrThrow(commentId);
        comment.addHeart(userId, heartId);
    }

    public void removeCommentHeart(final String commentId, final String userId) {
        final Comment comment = findCommentOrThrow(commentId);
        comment.removeHeart(userId);
    }

    private Comment findCommentOrThrow(final String commentId) {
        return comments.stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));
    }

    public Heart addHeart(final String heartId, final String userId) {
        if (isDeleted()) {
            throw new IllegalStateException("삭제된 피드에 좋아요를 누를 수 없습니다");
        }
        final boolean alreadyHearted = hearts.stream()
                .anyMatch(heart -> heart.getUserId().equals(userId) && !heart.isDeleted());
        if (alreadyHearted) {
            throw new IllegalStateException("이미 좋아요를 누르셨습니다");
        }
        final Heart heart = new Heart(heartId, this.id, userId, LocalDateTime.now(), null);
        hearts.add(heart);
        return heart;
    }

    public void removeHeart(final String userId) {
        hearts.stream()
                .filter(heart -> heart.getUserId().equals(userId) && !heart.isDeleted())
                .findFirst()
                .ifPresent(Heart::delete);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean canEditBy(final String userId) {
        return this.userId.equals(userId);
    }

    public boolean canDeleteBy(final String userId, final boolean isStaff) {
        return this.userId.equals(userId) || isStaff;
    }

    public List<Comment> getActiveComments() {
        return comments.stream()
                .filter(comment -> !comment.isDeleted())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Heart> getActiveHearts() {
        return hearts.stream()
                .filter(heart -> !heart.isDeleted())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public int getHeartsCount() {
        return (int) hearts.stream()
                .filter(heart -> !heart.isDeleted())
                .count();
    }

    public int getCommentsCount() {
        return (int) comments.stream()
                .filter(comment -> !comment.isDeleted())
                .count();
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getClubId() {
        return clubId;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public FeedType getFeedType() {
        return feedType;
    }

    public FeedImages getImages() {
        return images;
    }

    public FeedVideos getVideos() {
        return videos;
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public List<Heart> getHearts() {
        return Collections.unmodifiableList(hearts);
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
