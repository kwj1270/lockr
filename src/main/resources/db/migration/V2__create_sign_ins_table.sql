-- sign_ins definition
CREATE TABLE `sign_ins`
(
    `id`            CHAR(128)      NOT NULL COMMENT '로그인 고유 ID (ULID)',
    `user_id`       CHAR(128)      NOT NULL COMMENT '사용자 고유 ID (ULID)',
    `provider_id`   VARCHAR(255)  NOT NULL COMMENT '외부 인증 제공업체의 사용자 ID',
    `provider_type` VARCHAR(50)   NOT NULL COMMENT '외부 인증 제공업체 유형 (e.g., GOOGLE, APPLE)',
    `device_id`     VARCHAR(255)  NULL COMMENT '로그인한 기기의 ID',
    `device_info`   VARCHAR(512)  NULL COMMENT '로그인한 기기 정보',
    `ip_address`    VARCHAR(45)   NULL COMMENT '로그인한 IP 주소',
    `user_agent`    VARCHAR(512)  NULL COMMENT '사용자 에이전트 문자열',
    `created_at`    DATETIME(6)   NOT NULL COMMENT '로그인 시각',
    PRIMARY KEY (`id`)
)
COLLATE = utf8mb4_unicode_ci;

CREATE INDEX `idx_sign_ins_user_id` ON `sign_ins` (`user_id`);
CREATE INDEX `idx_provider_id_type` ON `sign_ins` (`provider_id`, `provider_type`);
