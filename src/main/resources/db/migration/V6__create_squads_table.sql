use lockr;
CREATE TABLE `squads`
(
    `id`         CHAR(128)   NOT NULL COMMENT '스쿼드 식별키' PRIMARY KEY,
    `club_id`    CHAR(128)   NOT NULL COMMENT '팀 식별키',
    `created_at` DATETIME(6) NOT NULL COMMENT '생성 시각',
    `updated_at` DATETIME(6) NOT NULL COMMENT '변경 시각',
    `deleted_at` DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci;
CREATE UNIQUE INDEX idx_squad_club_id ON `squads` (`club_id`);

CREATE TABLE `squad_players`
(
    `id`            CHAR(128)    NOT NULL COMMENT '스쿼드 선수 식별키' PRIMARY KEY,
    `user_id`     CHAR(128)    NOT NULL COMMENT '회원 식별키',
    `squad_id`      CHAR(128)    NOT NULL COMMENT '스쿼드 식별키',
    `profile_image` VARCHAR(255) NULL COMMENT '선수 프로필',
    `name`          VARCHAR(100) NULL COMMENT '선수 이름',
    `positions`     VARCHAR(255) NULL COMMENT '포지션 목록 (콤마 구분)',
    `birth_date`    CHAR(8)         NULL COMMENT '생년월일(YYYYMMDD)',
    `height`        VARCHAR(10)  NULL COMMENT '키',
    `weight`        VARCHAR(10)  NULL COMMENT '몸무게',
    `foot`          VARCHAR(10)  NULL COMMENT '주발' CHECK (foot IN ('LEFT', 'RIGHT', 'BOTH')),
    `back_number`   INT          NOT NULL COMMENT '등번호',
    `created_at`    DATETIME(6)  NOT NULL COMMENT '생성 시각',
    `updated_at`    DATETIME(6)  NOT NULL COMMENT '변경 시각',
    `deleted_at`    DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_squad_player_user_id ON `squad_players` (`user_id`);
CREATE INDEX idx_squad_player_squad_id ON `squad_players` (`squad_id`);
