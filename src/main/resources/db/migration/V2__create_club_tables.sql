-- ============================================================
-- V2: 클럽 테이블 (clubs, members, user_pinned_clubs)
-- ============================================================

-- 클럽
CREATE TABLE clubs
(
    id                   CHAR(128)    NOT NULL COMMENT '클럽 식별키' PRIMARY KEY,
    found_user_id        CHAR(128)    NOT NULL COMMENT '클럽 창단 사용자 식별키',
    name                 VARCHAR(50)  NOT NULL COMMENT '클럽 이름',
    sport_type           VARCHAR(50)  NOT NULL COMMENT '스포츠 타입',
    city                 VARCHAR(100) NOT NULL COMMENT '광역지방자치단체',
    district             VARCHAR(100) NOT NULL COMMENT '기초지방자치단체',
    description          VARCHAR(255) NOT NULL COMMENT '클럽 설명',
    profile_image_url    VARCHAR(500) NULL COMMENT '프로필 이미지 URL',
    background_image_url VARCHAR(500) NULL COMMENT '배경 이미지 URL',
    is_public            BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '공개 여부',
    join_method          VARCHAR(30)  NOT NULL DEFAULT 'APPROVAL_REQUIRED' COMMENT '가입 방식',
    created_at           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at           DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '클럽';

CREATE UNIQUE INDEX idx_clubs_name ON clubs (name);

-- 클럽 멤버
CREATE TABLE members
(
    id            CHAR(128)    NOT NULL COMMENT '회원 식별키' PRIMARY KEY,
    user_id       CHAR(128)    NOT NULL COMMENT '사용자 식별키',
    club_id       CHAR(128)    NOT NULL COMMENT '클럽 식별키',
    member_role   VARCHAR(255) NOT NULL COMMENT '회원 권한',
    name          VARCHAR(100) NULL COMMENT '멤버 이름',
    profile_image VARCHAR(500) NULL COMMENT '클럽별 프로필 이미지 URL',
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at    DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '클럽 멤버';

CREATE INDEX idx_members_user_club ON members (user_id, club_id, deleted_at);

-- 사용자 핀 클럽
CREATE TABLE user_pinned_clubs
(
    id               CHAR(128)   NOT NULL COMMENT '핀 식별키' PRIMARY KEY,
    user_id          CHAR(128)   NOT NULL COMMENT '사용자 식별키',
    club_id          CHAR(128)   NOT NULL COMMENT '클럽 식별키',
    pin_order        INT         NOT NULL COMMENT '핀 순서 (1 또는 2)',
    background_color VARCHAR(20) NULL COMMENT '카드 배경색',
    created_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '사용자 핀 클럽';

CREATE UNIQUE INDEX idx_user_pinned_clubs_user_club ON user_pinned_clubs (user_id, club_id);
CREATE INDEX idx_user_pinned_clubs_user ON user_pinned_clubs (user_id);
