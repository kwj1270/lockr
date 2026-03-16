package com.official.lockr.domain.shorts.domain;

import com.official.lockr.domain.shorts.domain.event.UploadedShortsEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Shorts extends AggregateRoot {

    private static final int AUTO_HIDE_THRESHOLD = 3;

    private final String id;
    private final String clubId;
    private final String userId;
    private final String title;
    private final String description;
    private final String videoUrl;
    private final String thumbnailUrl;
    private final int duration;
    private long viewCount;
    private List<ShortsHeart> hearts;
    private List<ShortsComment> comments;
    private ModerationStatus moderationStatus;
    private List<ShortsReport> reports;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Shorts(final String id, final String clubId, final String userId, final String title,
                  final String description, final String videoUrl, final String thumbnailUrl,
                  final int duration, final long viewCount, final List<ShortsHeart> hearts,
                  final List<ShortsComment> comments, final ModerationStatus moderationStatus,
                  final List<ShortsReport> reports, final LocalDateTime createdAt,
                  final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.clubId = clubId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
        this.viewCount = viewCount;
        this.hearts = hearts != null ? hearts : new ArrayList<>();
        this.comments = comments != null ? comments : new ArrayList<>();
        this.moderationStatus = moderationStatus != null ? moderationStatus : ModerationStatus.ACTIVE;
        this.reports = reports != null ? reports : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Shorts create(final String id, final String clubId, final String userId,
                                 final String title, final String description,
                                 final String videoUrl, final String thumbnailUrl, final int duration) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다");
        }
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new IllegalArgumentException("비디오 URL은 필수입니다");
        }

        final LocalDateTime now = LocalDateTime.now();
        final Shorts shorts = new Shorts(id, clubId, userId, title, description, videoUrl, thumbnailUrl,
                duration, 0, new ArrayList<>(), new ArrayList<>(), ModerationStatus.ACTIVE, new ArrayList<>(), now, now, null);
        shorts.addEvent(new UploadedShortsEvent(id, clubId, userId, title, now));
        return shorts;
    }

    public void delete(final String userId, final boolean isStaff) {
        final boolean isAuthor = this.userId.equals(userId);

        if (!isAuthor && !isStaff) {
            throw new IllegalStateException("작성자 또는 운영진만 삭제할 수 있습니다");
        }

        this.deletedAt = LocalDateTime.now();
        this.comments.forEach(comment -> comment.delete(comment.getUserId()));
        this.hearts.forEach(ShortsHeart::delete);
    }

    public ShortsHeart addHeart(final String heartId, final String userId) {
        if (isDeleted()) {
            throw new IllegalStateException("삭제된 숏츠에 좋아요를 누를 수 없습니다");
        }
        final boolean alreadyHearted = hearts.stream()
                .anyMatch(heart -> heart.getUserId().equals(userId) && !heart.isDeleted());
        if (alreadyHearted) {
            throw new IllegalStateException("이미 좋아요를 누르셨습니다");
        }
        final ShortsHeart heart = new ShortsHeart(heartId, this.id, userId, LocalDateTime.now(), null);
        hearts.add(heart);
        return heart;
    }

    public void removeHeart(final String userId) {
        hearts.stream()
                .filter(heart -> heart.getUserId().equals(userId) && !heart.isDeleted())
                .findFirst()
                .ifPresent(ShortsHeart::delete);
    }

    public ShortsComment addComment(final String commentId, final String userId, final String content) {
        if (isDeleted()) {
            throw new IllegalStateException("삭제된 숏츠에 댓글을 작성할 수 없습니다");
        }
        final ShortsComment comment = ShortsComment.create(commentId, this.id, userId, content);
        comments.add(comment);
        return comment;
    }

    public void deleteComment(final String commentId, final String userId) {
        final ShortsComment comment = comments.stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));
        comment.delete(userId);
    }

    public void incrementViewCount() {
        if (isDeleted()) {
            throw new IllegalStateException("삭제된 숏츠의 조회수를 증가시킬 수 없습니다");
        }
        this.viewCount++;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public List<ShortsHeart> getActiveHearts() {
        return hearts.stream()
                .filter(heart -> !heart.isDeleted())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<ShortsComment> getActiveComments() {
        return comments.stream()
                .filter(comment -> !comment.isDeleted())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ShortsReport report(final String reportId, final String userId, final ReportReason reason, final String detail) {
        if (isDeleted()) {
            throw new IllegalStateException("삭제된 숏츠는 신고할 수 없습니다");
        }
        final boolean alreadyReported = reports.stream()
                .anyMatch(report -> report.getUserId().equals(userId));
        if (alreadyReported) {
            throw new IllegalStateException("이미 신고한 숏츠입니다");
        }
        final ShortsReport report = new ShortsReport(reportId, this.id, userId, reason, detail, LocalDateTime.now());
        reports.add(report);
        if (reports.size() >= AUTO_HIDE_THRESHOLD) {
            this.moderationStatus = ModerationStatus.HIDDEN;
        }
        return report;
    }

    public void restore() {
        this.moderationStatus = ModerationStatus.ACTIVE;
    }

    public boolean isHidden() {
        return moderationStatus == ModerationStatus.HIDDEN;
    }

    public List<ShortsReport> getActiveReports() {
        return new ArrayList<>(reports);
    }

    public int getReportsCount() {
        return reports.size();
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

    public String getId() {
        return id;
    }

    public String getClubId() {
        return clubId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getDuration() {
        return duration;
    }

    public long getViewCount() {
        return viewCount;
    }

    public List<ShortsHeart> getHearts() {
        return Collections.unmodifiableList(hearts);
    }

    public List<ShortsComment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public List<ShortsReport> getReports() {
        return Collections.unmodifiableList(reports);
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
