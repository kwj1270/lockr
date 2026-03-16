-- ============================================================
-- V11: 숏츠 테이블 (shorts, hearts, comments, reports)
-- ============================================================

-- 숏츠
CREATE TABLE shorts
(
    id                VARCHAR(26)  NOT NULL COMMENT '숏츠 ID (ULID)' PRIMARY KEY,
    club_id           VARCHAR(26)  NOT NULL COMMENT '클럽 ID',
    user_id           VARCHAR(26)  NOT NULL COMMENT '업로더 ID',
    title             VARCHAR(100) NOT NULL COMMENT '숏츠 제목',
    description       TEXT         NULL COMMENT '숏츠 설명',
    video_url         VARCHAR(500) NOT NULL COMMENT '비디오 URL',
    thumbnail_url     VARCHAR(500) NULL COMMENT '썸네일 URL',
    duration          INT          NOT NULL DEFAULT 0 COMMENT '영상 길이 (초)',
    view_count        BIGINT       NOT NULL DEFAULT 0 COMMENT '조회수',
    moderation_status VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '모더레이션 상태 (ACTIVE, HIDDEN)',
    created_at        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at        DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '숏츠';

CREATE INDEX idx_shorts_club_id ON shorts (club_id);
CREATE INDEX idx_shorts_user_id ON shorts (user_id);
CREATE INDEX idx_shorts_created_at ON shorts (created_at);
CREATE INDEX idx_shorts_club_deleted ON shorts (club_id, deleted_at);
CREATE INDEX idx_shorts_deleted_created ON shorts (deleted_at, created_at);
CREATE INDEX idx_shorts_moderation_status ON shorts (moderation_status);

-- 숏츠 좋아요
CREATE TABLE shorts_hearts
(
    id         VARCHAR(26) NOT NULL COMMENT '좋아요 ID (ULID)' PRIMARY KEY,
    shorts_id  VARCHAR(26) NOT NULL COMMENT '숏츠 ID',
    user_id    VARCHAR(26) NOT NULL COMMENT '사용자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    deleted_at DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '숏츠 좋아요';

CREATE INDEX idx_shorts_hearts_shorts_id ON shorts_hearts (shorts_id);
CREATE INDEX idx_shorts_hearts_user_id ON shorts_hearts (user_id);
CREATE INDEX idx_shorts_hearts_shorts_user ON shorts_hearts (shorts_id, user_id);
CREATE INDEX idx_shorts_hearts_shorts_deleted ON shorts_hearts (shorts_id, deleted_at);

-- 숏츠 댓글
CREATE TABLE shorts_comments
(
    id         VARCHAR(26) NOT NULL COMMENT '댓글 ID (ULID)' PRIMARY KEY,
    shorts_id  VARCHAR(26) NOT NULL COMMENT '숏츠 ID',
    user_id    VARCHAR(26) NOT NULL COMMENT '작성자 ID',
    content    TEXT        NOT NULL COMMENT '댓글 내용',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '숏츠 댓글';

CREATE INDEX idx_shorts_comments_shorts_id ON shorts_comments (shorts_id);
CREATE INDEX idx_shorts_comments_user_id ON shorts_comments (user_id);
CREATE INDEX idx_shorts_comments_shorts_deleted ON shorts_comments (shorts_id, deleted_at);

-- 숏츠 신고
CREATE TABLE shorts_reports
(
    id         VARCHAR(26) NOT NULL COMMENT '신고 ID (ULID)' PRIMARY KEY,
    shorts_id  VARCHAR(26) NOT NULL COMMENT '숏츠 ID',
    user_id    VARCHAR(26) NOT NULL COMMENT '신고자 ID',
    reason     VARCHAR(50) NOT NULL COMMENT '신고 사유 (INAPPROPRIATE_CONTENT, SPAM, HARASSMENT, OTHER)',
    detail     TEXT        NULL COMMENT '상세 사유',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    UNIQUE KEY uk_shorts_reports_shorts_user (shorts_id, user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT = '숏츠 신고';

CREATE INDEX idx_shorts_reports_shorts_id ON shorts_reports (shorts_id);
CREATE INDEX idx_shorts_reports_user_id ON shorts_reports (user_id);
CREATE INDEX idx_shorts_reports_shorts_user ON shorts_reports (shorts_id, user_id);
