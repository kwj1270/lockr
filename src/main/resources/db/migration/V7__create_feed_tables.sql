-- ============================================================
-- V7: 피드 테이블 (feeds, images, videos, hearts, comments, reports, polls)
-- ============================================================

-- 피드
CREATE TABLE feeds
(
    id         VARCHAR(26) NOT NULL COMMENT '피드 ID (ULID)' PRIMARY KEY,
    club_id    VARCHAR(26) NOT NULL COMMENT '클럽 ID',
    user_id    VARCHAR(26) NOT NULL COMMENT '작성자 ID',
    title      VARCHAR(50) NULL COMMENT '피드 제목',
    content    TEXT        NOT NULL COMMENT '피드 내용',
    feed_type  VARCHAR(50) NOT NULL COMMENT '피드 타입 (GENERAL, NOTICE)',
    metadata   JSON        NULL COMMENT '타입별 메타데이터',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '피드';

CREATE INDEX idx_feeds_club_id ON feeds (club_id);
CREATE INDEX idx_feeds_user_id ON feeds (user_id);
CREATE INDEX idx_feeds_feed_type ON feeds (feed_type);
CREATE INDEX idx_feeds_club_deleted ON feeds (club_id, deleted_at);
CREATE INDEX idx_feeds_created_at ON feeds (created_at);

-- 피드 이미지
CREATE TABLE feed_images
(
    id         VARCHAR(26)  NOT NULL COMMENT '이미지 ID (ULID)' PRIMARY KEY,
    feed_id    VARCHAR(26)  NOT NULL COMMENT '피드 ID',
    user_id    VARCHAR(26)  NOT NULL COMMENT '업로드 사용자 ID',
    url        VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    deleted_at DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '피드 이미지';

CREATE INDEX idx_feed_images_feed_id ON feed_images (feed_id);
CREATE INDEX idx_feed_images_user_id ON feed_images (user_id);

-- 피드 비디오
CREATE TABLE feed_videos
(
    id         VARCHAR(26)  NOT NULL COMMENT '비디오 ID (ULID)' PRIMARY KEY,
    feed_id    VARCHAR(26)  NOT NULL COMMENT '피드 ID',
    user_id    VARCHAR(26)  NOT NULL COMMENT '업로드 사용자 ID',
    url        VARCHAR(500) NOT NULL COMMENT '비디오 URL',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    deleted_at DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '피드 비디오';

CREATE INDEX idx_feed_videos_feed_id ON feed_videos (feed_id);
CREATE INDEX idx_feed_videos_user_id ON feed_videos (user_id);

-- 피드 좋아요
CREATE TABLE feed_hearts
(
    id         VARCHAR(26) NOT NULL COMMENT '좋아요 ID (ULID)' PRIMARY KEY,
    feed_id    VARCHAR(26) NOT NULL COMMENT '피드 ID',
    user_id    VARCHAR(26) NOT NULL COMMENT '사용자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    deleted_at DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '피드 좋아요';

CREATE INDEX idx_feed_hearts_feed_id ON feed_hearts (feed_id);
CREATE INDEX idx_feed_hearts_user_id ON feed_hearts (user_id);
CREATE INDEX idx_feed_hearts_feed_user ON feed_hearts (feed_id, user_id);
CREATE INDEX idx_feed_hearts_feed_deleted ON feed_hearts (feed_id, deleted_at);

-- 댓글
CREATE TABLE comments
(
    id                VARCHAR(26) NOT NULL COMMENT '댓글 ID (ULID)' PRIMARY KEY,
    feed_id           VARCHAR(26) NOT NULL COMMENT '피드 ID',
    user_id           VARCHAR(26) NOT NULL COMMENT '작성자 ID',
    content           TEXT        NOT NULL COMMENT '댓글 내용',
    parent_comment_id VARCHAR(26) NULL COMMENT '부모 댓글 ID (대댓글)',
    created_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at        DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '댓글';

CREATE INDEX idx_comments_feed_id ON comments (feed_id);
CREATE INDEX idx_comments_user_id ON comments (user_id);
CREATE INDEX idx_comments_feed_deleted ON comments (feed_id, deleted_at);
CREATE INDEX idx_comments_parent ON comments (parent_comment_id);

-- 댓글 이미지
CREATE TABLE comment_images
(
    id         VARCHAR(26)  NOT NULL COMMENT '이미지 ID (ULID)' PRIMARY KEY,
    comment_id VARCHAR(26)  NOT NULL COMMENT '댓글 ID',
    user_id    VARCHAR(26)  NOT NULL COMMENT '업로드 사용자 ID',
    url        VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    deleted_at DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '댓글 이미지';

CREATE INDEX idx_comment_images_comment_id ON comment_images (comment_id);
CREATE INDEX idx_comment_images_user_id ON comment_images (user_id);

-- 댓글 비디오
CREATE TABLE comment_videos
(
    id         VARCHAR(26)  NOT NULL COMMENT '비디오 ID (ULID)' PRIMARY KEY,
    comment_id VARCHAR(26)  NOT NULL COMMENT '댓글 ID',
    user_id    VARCHAR(26)  NOT NULL COMMENT '업로드 사용자 ID',
    url        VARCHAR(500) NOT NULL COMMENT '비디오 URL',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    deleted_at DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '댓글 비디오';

CREATE INDEX idx_comment_videos_comment_id ON comment_videos (comment_id);
CREATE INDEX idx_comment_videos_user_id ON comment_videos (user_id);

-- 댓글 좋아요
CREATE TABLE comment_hearts
(
    id         VARCHAR(26) NOT NULL COMMENT '좋아요 ID (ULID)' PRIMARY KEY,
    comment_id VARCHAR(26) NOT NULL COMMENT '댓글 ID',
    user_id    VARCHAR(26) NOT NULL COMMENT '사용자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    deleted_at DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '댓글 좋아요';

CREATE INDEX idx_comment_hearts_comment_id ON comment_hearts (comment_id);
CREATE INDEX idx_comment_hearts_user_id ON comment_hearts (user_id);
CREATE INDEX idx_comment_hearts_comment_user ON comment_hearts (comment_id, user_id);
CREATE INDEX idx_comment_hearts_comment_deleted ON comment_hearts (comment_id, deleted_at);

-- 피드 신고
CREATE TABLE feed_reports
(
    id               VARCHAR(26) NOT NULL COMMENT '신고 ID (ULID)' PRIMARY KEY,
    feed_id          VARCHAR(26) NOT NULL COMMENT '피드 ID',
    club_id          VARCHAR(26) NOT NULL COMMENT '클럽 ID',
    reporter_user_id VARCHAR(26) NOT NULL COMMENT '신고자 ID',
    reason           TEXT        NULL COMMENT '신고 사유',
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '처리 상태',
    created_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    UNIQUE KEY uk_feed_reports_feed_reporter (feed_id, reporter_user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT = '피드 신고';

CREATE INDEX idx_feed_reports_status ON feed_reports (status);
CREATE INDEX idx_feed_reports_club ON feed_reports (club_id);
CREATE INDEX idx_feed_reports_feed ON feed_reports (feed_id);

-- 투표
CREATE TABLE polls
(
    id             VARCHAR(26)  NOT NULL COMMENT '투표 ID (ULID)' PRIMARY KEY,
    feed_id        VARCHAR(26)  NOT NULL COMMENT '피드 ID',
    club_id        VARCHAR(26)  NOT NULL COMMENT '클럽 ID',
    title          VARCHAR(200) NOT NULL COMMENT '투표 제목',
    allow_multiple TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '복수 선택 허용 여부',
    anonymous      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '익명 투표 여부',
    deadline       DATETIME(6)  NULL COMMENT '마감 시간',
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at     DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '투표';

CREATE INDEX idx_polls_feed ON polls (feed_id);
CREATE INDEX idx_polls_club ON polls (club_id);

-- 투표 옵션
CREATE TABLE poll_options
(
    id         VARCHAR(26)  NOT NULL COMMENT '옵션 ID (ULID)' PRIMARY KEY,
    poll_id    VARCHAR(26)  NOT NULL COMMENT '투표 ID',
    content    VARCHAR(500) NOT NULL COMMENT '옵션 내용',
    sort_order INT          NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    deleted_at DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '투표 옵션';

CREATE INDEX idx_poll_options_poll ON poll_options (poll_id);

-- 투표 참여
CREATE TABLE poll_votes
(
    id             VARCHAR(26) NOT NULL COMMENT '투표 참여 ID (ULID)' PRIMARY KEY,
    poll_id        VARCHAR(26) NOT NULL COMMENT '투표 ID',
    poll_option_id VARCHAR(26) NOT NULL COMMENT '선택 옵션 ID',
    user_id        VARCHAR(26) NOT NULL COMMENT '투표자 ID',
    created_at     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    deleted_at     DATETIME(6) NULL COMMENT '삭제 시각',
    UNIQUE KEY uk_poll_votes_option_user (poll_option_id, user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT = '투표 참여';

CREATE INDEX idx_poll_votes_poll ON poll_votes (poll_id);
CREATE INDEX idx_poll_votes_user ON poll_votes (user_id);
