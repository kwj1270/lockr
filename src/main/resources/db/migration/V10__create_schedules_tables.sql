-- Schedules 테이블
CREATE TABLE schedules
(
    id                VARCHAR(26) PRIMARY KEY COMMENT '일정 ID (ULID)',
    club_id           VARCHAR(26)  NOT NULL COMMENT '클럽 ID',
    title             VARCHAR(255) NOT NULL COMMENT '일정 제목',
    content           TEXT COMMENT '일정 내용',
    location          VARCHAR(255) COMMENT '일정 장소',
    schedule_time     DATETIME     NOT NULL COMMENT '일정 시간',
    schedule_type     VARCHAR(50)  NOT NULL COMMENT '일정 타입 (MATCH, TRAINING, HAPPY_HOUR)',
    detail_type       VARCHAR(50) COMMENT '상세 타입 (MatchDetail, TrainingDetail 등)',
    detail_data       VARCHAR(255) COMMENT '타입별 상세 데이터 (JSON)',
    status            VARCHAR(50)  NOT NULL COMMENT '일정 상태 (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    deleted_at        DATETIME COMMENT '삭제 시간',
    INDEX idx_club_id (club_id),
    INDEX idx_schedule_time (schedule_time),
    INDEX idx_club_schedule_time (club_id, schedule_time),
    INDEX idx_status (status)
) COLLATE = utf8mb4_unicode_ci COMMENT ='일정 테이블';

-- Attendances 테이블
CREATE TABLE attendances
(
    id           VARCHAR(26) PRIMARY KEY COMMENT '참석 정보 ID (ULID)',
    schedule_id  VARCHAR(26) NOT NULL COMMENT '일정 ID',
    user_id      VARCHAR(26) NOT NULL COMMENT '사용자 ID',
    status       VARCHAR(50) NOT NULL COMMENT '참석 상태 (ATTENDING, NOT_ATTENDING, TENTATIVE, NO_RESPONSE)',
    reason       TEXT COMMENT '불참/지각 사유',
    responded_at DATETIME COMMENT '응답 시간',
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_schedule_user (schedule_id, user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT ='참석 정보 테이블';
