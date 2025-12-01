use lockr;

CREATE TABLE `sign_in`
(
    `id`            CHAR(26)     NOT NULL COMMENT '로그인 고유 ID (ULID)' PRIMARY KEY,
    `user_id`       CHAR(26)     NOT NULL COMMENT '사용자 고유 ID (ULID)',
    `device_id`    VARCHAR(255) NOT NULL COMMENT '기기의 ID',
    `device_name`  VARCHAR(255) NOT NULL COMMENT '기기 이름',
    `device_os`    VARCHAR(50)  NOT NULL COMMENT '기기 OS',
    `ip_address`    VARCHAR(45)  NULL COMMENT '로그인한 IP 주소',
    `user_agent`    VARCHAR(512) NULL COMMENT '사용자 에이전트',
    `created_at`    DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) COMMENT '로그인 시각'
) COLLATE = utf8mb4_unicode_ci;
CREATE INDEX `idx_sign_in_user_id` ON `sign_in` (`user_id`);

CREATE TABLE `sign_in_tokens`
(
    `id`           CHAR(26)     NOT NULL COMMENT '토큰 고유 ID (ULID)' PRIMARY KEY,
    `user_id`       CHAR(26)     NOT NULL COMMENT '사용자 고유 ID (ULID)',
    `sign_in_id`   CHAR(26)     NOT NULL COMMENT '로그인 고유 ID (ULID)',
    `token`        VARCHAR(255) UNIQUE NOT NULL COMMENT '자동 로그인 토큰',
    `expires_at`   DATETIME(6)  NOT NULL COMMENT '만료 시각',
    `created_at`   DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    `deleted_at`   DATETIME(6)  NULL COMMENT '토큰 무효화 시각',
    INDEX idx_token (token),
    INDEX idx_expires_at (expires_at)
);
