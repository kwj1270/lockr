-- 1. 라인업 테이블 (기존 유지 + 소폭 수정)
CREATE TABLE `lineups`
(
    `id`         CHAR(128)    NOT NULL COMMENT '포메이션 식별키' PRIMARY KEY,
    `club_id`    CHAR(128)    NOT NULL COMMENT '클럽 식별키',
    `name`       VARCHAR(100) NOT NULL COMMENT '포메이션 이름 (A, B, C)',
    `formation`  VARCHAR(20)  NOT NULL COMMENT '포메이션 타입 (4-4-2, 4-3-3 등)',
    `created_at` DATETIME(6)  NOT NULL COMMENT '생성 시각',
    `updated_at` DATETIME(6)  NOT NULL COMMENT '변경 시각',
    `deleted_at` DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_formation_club_id ON `lineups` (`club_id`);
CREATE INDEX idx_formation_deleted_at ON `lineups` (`deleted_at`);

-- 2. 라인업 선수 배치 테이블 (field_players + bench_players 통합)
CREATE TABLE `lineup_slots`
(
    `id`              CHAR(128)   NOT NULL COMMENT '배치 식별키' PRIMARY KEY,
    `lineup_id`       CHAR(128)   NOT NULL COMMENT '라인업 식별키',
    `squad_player_id` CHAR(128)   NOT NULL COMMENT '스쿼드 선수 식별키',
    `slot_type`       VARCHAR(20) NOT NULL COMMENT '슬롯 타입 (starter, substitute)',
    `slot_index`      INT         NOT NULL COMMENT '슬롯 인덱스 (0~10: 선발, 0~6: 후보)',
    `created_at`      DATETIME(6) NOT NULL COMMENT '생성 시각',
    CONSTRAINT chk_slot_type CHECK (`slot_type` IN ('starter', 'substitute')),
    CONSTRAINT chk_starter_index CHECK (
        (`slot_type` = 'starter' AND `slot_index` BETWEEN 0 AND 10) OR
        (`slot_type` = 'substitute' AND `slot_index` BETWEEN 0 AND 6)
        )
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_lineup_positions_lineup_id ON `lineup_slots` (`lineup_id`);
CREATE INDEX idx_lineup_positions_squad_player_id ON `lineup_slots` (`squad_player_id`);
CREATE UNIQUE INDEX idx_lineup_positions_unique ON `lineup_slots` (`lineup_id`, `slot_type`, `slot_index`);
