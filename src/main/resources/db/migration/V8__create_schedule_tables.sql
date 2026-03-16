-- ============================================================
-- V8: 일정 테이블 (schedules, attendances, histories, comments)
-- ============================================================

-- 일정
CREATE TABLE schedules
(
    id               VARCHAR(26)  NOT NULL COMMENT '일정 ID (ULID)' PRIMARY KEY,
    club_id          VARCHAR(26)  NOT NULL COMMENT '클럽 ID',
    title            VARCHAR(255) NOT NULL COMMENT '일정 제목',
    content          TEXT         NULL COMMENT '일정 내용',
    location         TEXT         NULL COMMENT '일정 장소',
    schedule_time    DATETIME(6)  NOT NULL COMMENT '일정 시간',
    type             VARCHAR(50)  NOT NULL COMMENT '일정 타입 (MATCH, TRAINING, SOCIAL_EVENT)',
    detail_data      JSON         NULL COMMENT '타입별 상세 데이터',
    status           VARCHAR(50)  NOT NULL COMMENT '일정 상태 (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)',
    min_participants INT          NULL COMMENT '최소 참가자 수',
    max_participants INT          NULL COMMENT '최대 참가자 수',
    deadline_days    INT          NOT NULL DEFAULT 0 COMMENT '응답 마감 (일정 며칠 전)',
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at       DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '일정';

CREATE INDEX idx_schedules_club_id ON schedules (club_id);
CREATE INDEX idx_schedules_schedule_time ON schedules (schedule_time);
CREATE INDEX idx_schedules_club_schedule_time ON schedules (club_id, schedule_time);
CREATE INDEX idx_schedules_status ON schedules (status);

-- 참석 정보
CREATE TABLE attendances
(
    id          VARCHAR(26) NOT NULL COMMENT '참석 정보 ID (ULID)' PRIMARY KEY,
    schedule_id VARCHAR(26) NOT NULL COMMENT '일정 ID',
    user_id     VARCHAR(26) NOT NULL COMMENT '사용자 ID',
    status      VARCHAR(50) NOT NULL COMMENT '참석 상태 (ATTENDING, NOT_ATTENDING, NO_RESPONSE)',
    reason      TEXT        NULL COMMENT '불참 사유',
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at  DATETIME(6) NULL COMMENT '삭제 시각',
    UNIQUE KEY uk_attendances_schedule_user (schedule_id, user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT = '참석 정보';

CREATE INDEX idx_attendances_schedule_id ON attendances (schedule_id);
CREATE INDEX idx_attendances_user_id ON attendances (user_id);
CREATE INDEX idx_attendances_status ON attendances (status);

-- 참석 상태 변경 이력
CREATE TABLE schedule_attendance_histories
(
    id              VARCHAR(26) NOT NULL COMMENT '이력 ID (ULID)' PRIMARY KEY,
    schedule_id     VARCHAR(26) NOT NULL COMMENT '일정 ID',
    attendance_id   VARCHAR(26) NOT NULL COMMENT '참석 정보 ID',
    user_id         VARCHAR(26) NOT NULL COMMENT '대상 사용자 ID',
    changed_by      VARCHAR(26) NOT NULL COMMENT '변경한 사람 ID',
    changed_by_role VARCHAR(50) NOT NULL COMMENT '변경한 사람 역할 (OWNER, MANAGER, COACH, PLAYER)',
    previous_status VARCHAR(50) NOT NULL COMMENT '이전 상태',
    new_status      VARCHAR(50) NOT NULL COMMENT '변경된 상태',
    reason          TEXT        NULL COMMENT '변경 사유',
    changed_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '변경 시간',
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at      DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '참석 상태 변경 이력';

CREATE INDEX idx_schedule_attendance_histories_attendance ON schedule_attendance_histories (attendance_id);
CREATE INDEX idx_schedule_attendance_histories_schedule ON schedule_attendance_histories (schedule_id);
CREATE INDEX idx_schedule_attendance_histories_user ON schedule_attendance_histories (user_id);
CREATE INDEX idx_schedule_attendance_histories_changed_by ON schedule_attendance_histories (changed_by);
CREATE INDEX idx_schedule_attendance_histories_changed_at ON schedule_attendance_histories (changed_at);

-- 일정 댓글
CREATE TABLE schedule_comments
(
    id          VARCHAR(26) NOT NULL COMMENT '댓글 ID (ULID)' PRIMARY KEY,
    schedule_id VARCHAR(26) NOT NULL COMMENT '일정 ID',
    club_id     VARCHAR(26) NOT NULL COMMENT '클럽 ID',
    user_id     VARCHAR(26) NOT NULL COMMENT '작성자 ID',
    content     TEXT        NOT NULL COMMENT '댓글 내용',
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at  DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '일정 댓글';

CREATE INDEX idx_schedule_comments_schedule ON schedule_comments (schedule_id);
CREATE INDEX idx_schedule_comments_user ON schedule_comments (user_id);
CREATE INDEX idx_schedule_comments_club ON schedule_comments (club_id);
CREATE INDEX idx_schedule_comments_deleted ON schedule_comments (schedule_id, deleted_at);
