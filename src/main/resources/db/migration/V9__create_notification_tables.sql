-- ============================================================
-- V9: 알림 테이블 (notifications)
-- ============================================================

-- 알림
CREATE TABLE notifications
(
    id          VARCHAR(26)  NOT NULL COMMENT '알림 ID (ULID)' PRIMARY KEY,
    user_id     VARCHAR(26)  NOT NULL COMMENT '수신자 사용자 ID',
    club_id     VARCHAR(26)  NULL COMMENT '관련 클럽 ID',
    type        VARCHAR(50)  NOT NULL COMMENT '알림 타입 (SCHEDULE_LINK_REQUEST, SCHEDULE_UPDATED 등)',
    title       VARCHAR(255) NOT NULL COMMENT '알림 제목',
    content     TEXT         NOT NULL COMMENT '알림 내용',
    data        JSON         NULL COMMENT '알림 관련 데이터',
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '읽음 여부',
    action_type VARCHAR(50)  NULL COMMENT '액션 타입 (CREATE_LINKED_SCHEDULE, VIEW_SCHEDULE 등)',
    action_url  VARCHAR(500) NULL COMMENT '액션 URL',
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at  DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '알림';

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_club_id ON notifications (club_id);
CREATE INDEX idx_notifications_type ON notifications (type);
CREATE INDEX idx_notifications_is_read ON notifications (is_read);
CREATE INDEX idx_notifications_user_club ON notifications (user_id, club_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);
