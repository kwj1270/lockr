CREATE TABLE `matches`
(
    `id`                        CHAR(128)    NOT NULL COMMENT '매치 식별키' PRIMARY KEY,
    `home_club_id`              CHAR(128)    NOT NULL COMMENT '홈 클럽 식별키',
    `home_club_manager_user_id` CHAR(128)    NOT NULL COMMENT '홈 클럽 매니저 식별키',
    `away_club_id`              CHAR(128)    NOT NULL COMMENT '어웨이 클럽 식별키',
    `away_club_manager_user_id` CHAR(128)    NULL COMMENT '어웨이 클럽 매니저 식별키',
    `location`                  VARCHAR(255) NOT NULL COMMENT '경기 장소',
    `status`                    VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '매치 상태' CHECK (status IN ('PENDING', 'ACCEPTED', 'CANCELLED')),
    `match_date_time`           DATETIME(6)  NOT NULL COMMENT '매치 일시',
    `created_at`                DATETIME(6)  NOT NULL COMMENT '생성 시각',
    `updated_at`                DATETIME(6)  NOT NULL COMMENT '변경 시각',
    `deleted_at`                DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_match_home_club_id ON `matches` (`home_club_id`);
CREATE INDEX idx_match_away_club_id ON `matches` (`away_club_id`);
CREATE INDEX idx_match_status ON `matches` (`status`);
CREATE INDEX idx_match_date_time ON `matches` (`match_date_time`);
