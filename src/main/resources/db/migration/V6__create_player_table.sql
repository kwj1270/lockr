CREATE TABLE `squads`
(
    `id`         CHAR(128)   NOT NULL COMMENT '스쿼드 식별키' PRIMARY KEY,
    `team_id`    CHAR(128)   NOT NULL COMMENT '팀 식별키',
    `created_at` DATETIME(6) NOT NULL COMMENT '생성 시각',
    `updated_at` DATETIME(6) NOT NULL COMMENT '변경 시각',
    `deleted_at` DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci;
CREATE UNIQUE INDEX idx_squad_team_id ON `squads` (`team_id`);

CREATE TABLE `players`
(
    `id`            CHAR(128)    NOT NULL COMMENT '선수 식별키' PRIMARY KEY,
    `member_id`     CHAR(128)    NOT NULL COMMENT '회원 식별키',
    `squad_id`      CHAR(128)    NOT NULL COMMENT '스쿼드 식별키',
    `profile_image` VARCHAR(255) NULL COMMENT '선수 프로필',
    `name`          VARCHAR(100) NULL COMMENT '선수 이름',
    `nationality`   VARCHAR(50)  NULL COMMENT '국적',
    `positions`     VARCHAR(255) NULL COMMENT '포지션 목록 (콤마 구분)',
    `birth`         VARCHAR(10)  NULL COMMENT '생년월일',
    `height`        VARCHAR(10)  NULL COMMENT '키',
    `weight`        VARCHAR(10)  NULL COMMENT '몸무게',
    `foot`          VARCHAR(10)  NULL COMMENT '주발' CHECK (foot IN ('LEFT', 'RIGHT', 'BOTH')),
    `back_number`   INT          NOT NULL COMMENT '등번호',
    `player_type`   VARCHAR(20)  NOT NULL DEFAULT 'BASIC' COMMENT '선수 역할' CHECK (player_type IN ('BASIC', 'VICE_CAPTAIN', 'CAPTAIN')),
    `created_at`    DATETIME(6)  NOT NULL COMMENT '생성 시각',
    `updated_at`    DATETIME(6)  NOT NULL COMMENT '변경 시각',
    `deleted_at`    DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_player_member_id ON `players` (`member_id`);
CREATE INDEX idx_player_squad_id ON `players` (`squad_id`);
