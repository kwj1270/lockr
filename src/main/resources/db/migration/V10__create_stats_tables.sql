-- ============================================================
-- V10: 통계 테이블 (match_records, player_match_stats)
-- ============================================================

-- 매치 기록
CREATE TABLE match_records
(
    id             VARCHAR(26)  NOT NULL COMMENT '매치 기록 ID (ULID)' PRIMARY KEY,
    club_id        VARCHAR(26)  NOT NULL COMMENT '클럽 ID',
    schedule_id    VARCHAR(26)  NULL COMMENT '일정 ID',
    match_date     DATE         NOT NULL COMMENT '경기 날짜',
    opponent_name  VARCHAR(255) NOT NULL COMMENT '상대팀 이름',
    our_score      INT          NOT NULL DEFAULT 0 COMMENT '우리팀 득점',
    opponent_score INT          NOT NULL DEFAULT 0 COMMENT '상대팀 득점',
    result         VARCHAR(10)  NOT NULL COMMENT '경기 결과 (WIN, DRAW, LOSE)',
    recorded_by    VARCHAR(26)  NOT NULL COMMENT '기록자 ID',
    season         VARCHAR(20)  NULL COMMENT '시즌',
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at     DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '매치 기록';

CREATE INDEX idx_match_records_club_season ON match_records (club_id, season);
CREATE INDEX idx_match_records_club_date ON match_records (club_id, match_date DESC);

-- 선수별 매치 스탯
CREATE TABLE player_match_stats
(
    id              VARCHAR(26) NOT NULL COMMENT '스탯 ID (ULID)' PRIMARY KEY,
    match_record_id VARCHAR(26) NOT NULL COMMENT '매치 기록 ID',
    club_id         VARCHAR(26) NOT NULL COMMENT '클럽 ID',
    user_id         VARCHAR(26) NOT NULL COMMENT '선수 ID',
    goals           INT         NOT NULL DEFAULT 0 COMMENT '득점 수',
    assists         INT         NOT NULL DEFAULT 0 COMMENT '어시스트 수',
    is_mom          BOOLEAN     NOT NULL DEFAULT FALSE COMMENT 'MOM 여부',
    minutes_played  INT         NULL COMMENT '출전 시간 (분)',
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    UNIQUE KEY uk_player_match_stats_match_user (match_record_id, user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT = '선수별 매치 스탯';

CREATE INDEX idx_player_match_stats_club_user ON player_match_stats (club_id, user_id);
