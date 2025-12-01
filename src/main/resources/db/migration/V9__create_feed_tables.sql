-- Feeds 테이블
use lockr;
CREATE TABLE feeds
(
    id         VARCHAR(26) PRIMARY KEY COMMENT '피드 ID (ULID)',
    club_id    VARCHAR(26)  NOT NULL COMMENT '클럽 ID',
    user_id    VARCHAR(26)  NOT NULL COMMENT '작성자 ID',
    content    TEXT         NOT NULL COMMENT '피드 내용',
    feed_type  VARCHAR(50)  NOT NULL COMMENT '피드 타입 (GENERAL, NOTICE)',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    deleted_at DATETIME COMMENT '삭제 시간',
    INDEX idx_club_id (club_id),
    INDEX idx_user_id (user_id),
    INDEX idx_feed_type (feed_type),
    INDEX idx_club_deleted (club_id, deleted_at),
    INDEX idx_created_at (created_at)
) COLLATE = utf8mb4_unicode_ci COMMENT ='피드 테이블';

-- Feed Images 테이블
CREATE TABLE feed_images
(
    id         VARCHAR(26) PRIMARY KEY COMMENT '이미지 ID (ULID)',
    feed_id    VARCHAR(26)  NOT NULL COMMENT '피드 ID',
    user_id    VARCHAR(26)  NOT NULL COMMENT '업로드 사용자 ID',
    url        VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    deleted_at DATETIME COMMENT '삭제 시간',
    INDEX idx_feed_id (feed_id),
    INDEX idx_user_id (user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT ='피드 이미지 테이블';

-- Feed Videos 테이블
CREATE TABLE feed_videos
(
    id         VARCHAR(26) PRIMARY KEY COMMENT '비디오 ID (ULID)',
    feed_id    VARCHAR(26)  NOT NULL COMMENT '피드 ID',
    user_id    VARCHAR(26)  NOT NULL COMMENT '업로드 사용자 ID',
    url        VARCHAR(500) NOT NULL COMMENT '비디오 URL',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    deleted_at DATETIME COMMENT '삭제 시간',
    INDEX idx_feed_id (feed_id),
    INDEX idx_user_id (user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT ='피드 비디오 테이블';

-- Hearts 테이블 (피드 좋아요)
CREATE TABLE feed_hearts
(
    id         VARCHAR(26) PRIMARY KEY COMMENT '좋아요 ID (ULID)',
    feed_id    VARCHAR(26) NOT NULL COMMENT '피드 ID',
    user_id    VARCHAR(26) NOT NULL COMMENT '사용자 ID',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    deleted_at DATETIME COMMENT '삭제 시간',
    INDEX idx_feed_id (feed_id),
    INDEX idx_user_id (user_id),
    INDEX idx_feed_user (feed_id, user_id),
    INDEX idx_feed_deleted (feed_id, deleted_at)
) COLLATE = utf8mb4_unicode_ci COMMENT ='피드 좋아요 테이블';


-- Comments 테이블
CREATE TABLE comments
(
    id         VARCHAR(26) PRIMARY KEY COMMENT '댓글 ID (ULID)',
    feed_id    VARCHAR(26) NOT NULL COMMENT '피드 ID',
    user_id    VARCHAR(26) NOT NULL COMMENT '작성자 ID',
    content    TEXT        NOT NULL COMMENT '댓글 내용',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    deleted_at DATETIME COMMENT '삭제 시간',
    INDEX idx_feed_id (feed_id),
    INDEX idx_user_id (user_id),
    INDEX idx_feed_deleted (feed_id, deleted_at)
) COLLATE = utf8mb4_unicode_ci COMMENT ='댓글 테이블';

-- Comment Images 테이블
CREATE TABLE comment_images
(
    id         VARCHAR(26) PRIMARY KEY COMMENT '이미지 ID (ULID)',
    comment_id VARCHAR(26)  NOT NULL COMMENT '댓글 ID',
    user_id    VARCHAR(26)  NOT NULL COMMENT '업로드 사용자 ID',
    url        VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    deleted_at DATETIME COMMENT '삭제 시간',
    INDEX idx_comment_id (comment_id),
    INDEX idx_user_id (user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT ='댓글 이미지 테이블';

-- Comment Videos 테이블
CREATE TABLE comment_videos
(
    id         VARCHAR(26) PRIMARY KEY COMMENT '비디오 ID (ULID)',
    comment_id VARCHAR(26)  NOT NULL COMMENT '댓글 ID',
    user_id    VARCHAR(26)  NOT NULL COMMENT '업로드 사용자 ID',
    url        VARCHAR(500) NOT NULL COMMENT '비디오 URL',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    deleted_at DATETIME COMMENT '삭제 시간',
    INDEX idx_comment_id (comment_id),
    INDEX idx_user_id (user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT ='댓글 비디오 테이블';

-- Comment Hearts 테이블 (댓글 좋아요)
CREATE TABLE comment_hearts
(
    id         VARCHAR(26) PRIMARY KEY COMMENT '좋아요 ID (ULID)',
    comment_id VARCHAR(26) NOT NULL COMMENT '댓글 ID',
    user_id    VARCHAR(26) NOT NULL COMMENT '사용자 ID',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    deleted_at DATETIME COMMENT '삭제 시간',
    INDEX idx_comment_id (comment_id),
    INDEX idx_user_id (user_id),
    INDEX idx_comment_user (comment_id, user_id),
    INDEX idx_comment_deleted (comment_id, deleted_at)
) COLLATE = utf8mb4_unicode_ci COMMENT ='댓글 좋아요 테이블';
