-- teams definition
use lockr;
CREATE TABLE `clubs`
(
    `id`                   CHAR(128)    NOT NULL COMMENT '클럽 식별키' primary key,
    `found_user_id`        CHAR(128)    NOT NULL COMMENT '클럽 창단 사용자 식별키',
    `name`                 VARCHAR(50)  NOT NULL COMMENT '클럽 이름',
    `description`          VARCHAR(255) NOT NULL COMMENT '클럽 설명',
    `region`               VARCHAR(100) NOT NULL COMMENT '지역',
    `sport_type`           VARCHAR(50)  NOT NULL COMMENT '스포츠 타입',
    `profile_image_url`    VARCHAR(500) NULL COMMENT '프로필 이미지 URL',
    `background_image_url` VARCHAR(500) NULL COMMENT '배경 이미지 URL',
    `created_at`           DATETIME(6)  NOT NULL COMMENT '생성 시각',
    `updated_at`           DATETIME(6)  NOT NULL COMMENT '변경 시각',
    `deleted_at`           DATETIME(6)  NULL COMMENT '삭제 시각'
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
