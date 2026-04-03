-- ============================================================
-- V12: FCM 디바이스 토큰 테이블
-- ============================================================

CREATE TABLE fcm_tokens
(
    id         CHAR(26)     NOT NULL COMMENT 'FCM 토큰 식별키 (ULID)' PRIMARY KEY,
    user_id    CHAR(26)     NOT NULL COMMENT '사용자 식별키',
    token      VARCHAR(500) NOT NULL COMMENT 'FCM 디바이스 토큰',
    device_id  VARCHAR(255) NOT NULL COMMENT '디바이스 식별자',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    UNIQUE KEY uk_fcm_tokens_user_device (user_id, device_id),
    UNIQUE KEY uk_fcm_tokens_token (token)
) COLLATE = utf8mb4_unicode_ci COMMENT = 'FCM 디바이스 토큰';

CREATE INDEX idx_fcm_tokens_user_id ON fcm_tokens (user_id);
