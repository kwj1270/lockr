-- Match Records table: stores match results for a club
CREATE TABLE match_records (
    id               VARCHAR(26) PRIMARY KEY,
    club_id          VARCHAR(26) NOT NULL,
    schedule_id      VARCHAR(26),
    match_date       DATE NOT NULL,
    opponent_name    VARCHAR(255) NOT NULL,
    our_score        INT NOT NULL DEFAULT 0,
    opponent_score   INT NOT NULL DEFAULT 0,
    result           VARCHAR(10) NOT NULL,  -- WIN, DRAW, LOSE
    recorded_by      VARCHAR(26) NOT NULL,
    season           VARCHAR(20),
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at       DATETIME,

    INDEX idx_match_records_club_season (club_id, season),
    INDEX idx_match_records_club_date (club_id, match_date DESC)
) COLLATE = utf8mb4_unicode_ci;

-- Player match stats table: stores individual player performance per match
CREATE TABLE player_match_stats (
    id               VARCHAR(26) PRIMARY KEY,
    match_record_id  VARCHAR(26) NOT NULL,
    club_id          VARCHAR(26) NOT NULL,
    user_id          VARCHAR(26) NOT NULL,
    goals            INT NOT NULL DEFAULT 0,
    assists          INT NOT NULL DEFAULT 0,
    is_mom           BOOLEAN NOT NULL DEFAULT FALSE,
    minutes_played   INT,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_player_match_stats_club_user (club_id, user_id),
    UNIQUE KEY uk_player_match_stats_match_user (match_record_id, user_id)
) COLLATE = utf8mb4_unicode_ci;
