-- 1. schedules 테이블에 링크 관련 필드 추가

-- 2. notifications 테이블 생성
CREATE TABLE notifications
(
    id            VARCHAR(26) PRIMARY KEY COMMENT '알림 ID (ULID)',
    user_id       VARCHAR(26)  NOT NULL COMMENT '수신자 사용자 ID',
    club_id       VARCHAR(26) COMMENT '관련 클럽 ID',
    type          VARCHAR(50)  NOT NULL COMMENT '알림 타입 (SCHEDULE_LINK_REQUEST, SCHEDULE_UPDATED, etc)',
    title         VARCHAR(255) NOT NULL COMMENT '알림 제목',
    content       TEXT         NOT NULL COMMENT '알림 내용',
    data          JSON COMMENT '알림 관련 데이터 (scheduleId, opponentClubId 등)',
    is_read       BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '읽음 여부',
    action_type   VARCHAR(50) COMMENT '액션 타입 (CREATE_LINKED_SCHEDULE, VIEW_SCHEDULE, etc)',
    action_url    VARCHAR(500) COMMENT '액션 URL',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    deleted_at    DATETIME COMMENT '삭제 시간',

    -- 인덱스
    INDEX idx_user_id (user_id),
    INDEX idx_club_id (club_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_user_club (user_id, club_id),
    INDEX idx_created_at (created_at)
) COLLATE = utf8mb4_unicode_ci COMMENT ='알림 테이블';
