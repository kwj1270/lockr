-- Tactical Boards 테이블 (전술판)
CREATE TABLE `tactical_boards`
(
    `id`            CHAR(128)    NOT NULL COMMENT '전술판 식별키' PRIMARY KEY,
    `name`          VARCHAR(100) NOT NULL COMMENT '전술판 이름',
    `club_id`       CHAR(128)    NOT NULL COMMENT '클럽 식별키',
    `coach_user_id` CHAR(128)    NULL COMMENT '코치 유저 식별키',
    `created_at`    DATETIME(6)  NOT NULL COMMENT '생성 시각',
    `updated_at`    DATETIME(6)  NOT NULL COMMENT '변경 시각',
    `deleted_at`    DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_tactical_board_club_id ON `tactical_boards` (`club_id`);

-- Field Players 테이블 (필드 선수 11명)
CREATE TABLE `field_players`
(
    `id`                CHAR(128)   NOT NULL COMMENT '필드 선수 식별키(스쿼드랑 동일)' PRIMARY KEY,
    `tactical_board_id` CHAR(128)   NOT NULL COMMENT '전술판 식별키',
    `squad_player_id`         CHAR(128)   NOT NULL COMMENT '스쿼드 선수 식별키(스쿼드)',
    `position`          VARCHAR(10) NOT NULL COMMENT '포지션',
    `location_x`        INT         NOT NULL COMMENT '위치 X 좌표',
    `location_y`        INT         NOT NULL COMMENT '위치 Y 좌표',
    `is_captain`        BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '주장 여부'
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_field_players_tactical_board_id ON `field_players` (`tactical_board_id`);
CREATE INDEX idx_field_players_squad_player_id ON `field_players` (`squad_player_id`);

-- Bench Players 테이블 (벤치 선수 최대 7명)
CREATE TABLE `bench_players`
(
    `id`                CHAR(128)   NOT NULL COMMENT '벤치 선수 식별키' PRIMARY KEY,
    `tactical_board_id` CHAR(128)   NOT NULL COMMENT '전술판 식별키',
    `squad_player_id`         CHAR(128)   NOT NULL COMMENT '선수 식별키(스쿼드)',
    `position`          VARCHAR(10) NOT NULL COMMENT '포지션'
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_bench_players_tactical_board_id ON `bench_players` (`tactical_board_id`);
CREATE INDEX idx_bench_players_squad_player_id ON `bench_players` (`squad_player_id`);

-- None Selected Players 테이블 (미선발 선수)
CREATE TABLE `none_selected_players`
(
    `id`                CHAR(128)   NOT NULL COMMENT '미선발 선수 식별키' PRIMARY KEY,
    `tactical_board_id` CHAR(128)   NOT NULL COMMENT '전술판 식별키',
    `squad_player_id`         CHAR(128)   NOT NULL COMMENT '선수 식별키(스쿼드)',
    `position`          VARCHAR(10) NOT NULL COMMENT '포지션'
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_none_selected_players_tactical_board_id ON `none_selected_players` (`tactical_board_id`);
CREATE INDEX idx_none_selected_players_squad_player_id ON `none_selected_players` (squad_player_id);

-- Formation 테이블 (포메이션)
CREATE TABLE `formations`
(
    `id`                CHAR(128) NOT NULL COMMENT '포메이션 식별키' PRIMARY KEY,
    `tactical_board_id` CHAR(128) NOT NULL COMMENT '전술판 식별키',
    `formation`         CHAR(128) NOT NULL COMMENT '포메이션 이름'
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_formations_tactical_board_id ON `formations` (`tactical_board_id`);
