
CREATE TABLE `user_pinned_clubs`
(
    `id`         CHAR(128)   NOT NULL COMMENT '핀 식별키' PRIMARY KEY,
    `user_id`    CHAR(128)   NOT NULL COMMENT '사용자 식별키',
    `club_id`    CHAR(128)   NOT NULL COMMENT '클럽 식별키',
    `pin_order`  INT         NOT NULL COMMENT '핀 순서 (1 또는 2)',
    `background_color` VARCHAR(20) NULL COMMENT '카드 배경색',
    `created_at` DATETIME(6) NOT NULL COMMENT '생성 시각'
) COLLATE = utf8mb4_unicode_ci;

CREATE UNIQUE INDEX `idx_user_pinned_clubs_user_club` ON `user_pinned_clubs` (`user_id`, `club_id`);
CREATE INDEX `idx_user_pinned_clubs_user` ON `user_pinned_clubs` (`user_id`);
