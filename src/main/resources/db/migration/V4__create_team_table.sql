-- teams definition
CREATE TABLE `clubs`
(
    `id`          CHAR(128)    NOT NULL COMMENT '클럽 식별키' primary key,
    `name`        VARCHAR(50)  NOT NULL COMMENT '클럽 이름',
    `description` VARCHAR(255) NOT NULL COMMENT '클럽 설명',
    `created_at`  DATETIME(6)  NOT NULL COMMENT '생성 시각',
    `updated_at`  DATETIME(6)  NOT NULL COMMENT '변경 시각',
    `deleted_at`  DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci;

CREATE UNIQUE INDEX `idx_teams_name` ON `clubs` (`name`);

CREATE TABLE `members`
(
    `id`          CHAR(128)    NOT NULL COMMENT '회원 식별키' primary key,
    `user_id`     CHAR(128)    NOT NULL COMMENT '사용자 식별키',
    `member_role` VARCHAR(255) NOT NULL COMMENT '회원 권한',
    `club_id`     CHAR(128)    NOT NULL COMMENT '클럽 식별키',
    `created_at`  DATETIME(6)  NOT NULL COMMENT '생성 시각',
    `updated_at`  DATETIME(6)  NOT NULL COMMENT '변경 시각',
    `deleted_at`  DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci;
