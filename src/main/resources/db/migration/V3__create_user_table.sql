-- users definition
CREATE TABLE `users`
(
    `id`            CHAR(128)    NOT NULL COMMENT '사용자 식별키',
    `provider_id`   VARCHAR(255) NOT NULL COMMENT '외부 인증 제공업체의 사용자 ID',
    `provider_type` VARCHAR(50)  NOT NULL COMMENT '외부 인증 제공업체 유형 (e.g., GOOGLE, APPLE)',
    `created_at`    DATETIME(6)  NOT NULL COMMENT '생성 시각',
    `updated_at`    DATETIME(6)  NOT NULL COMMENT '변경 시각',
    `deleted_at`    DATETIME(6)  NULL COMMENT '삭제 시각',
    PRIMARY KEY (`id`)
) COLLATE = utf8mb4_unicode_ci;

CREATE UNIQUE INDEX `idx_users_user_id` ON `users` (`id`);
-- CREATE UNIQUE INDEX `idx_users_id_type` ON `users` (`provider_id`, `provider_type`);
