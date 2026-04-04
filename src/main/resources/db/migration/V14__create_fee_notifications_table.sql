CREATE TABLE fee_notifications (
    id         CHAR(26)    NOT NULL PRIMARY KEY COMMENT '알림 식별키 (ULID)',
    club_id    CHAR(26)    NOT NULL COMMENT '클럽 식별키',
    year       INT         NOT NULL COMMENT '대상 연도',
    month      INT         NOT NULL COMMENT '대상 월',
    sent_by    CHAR(26)    NOT NULL COMMENT '발송자 식별키',
    member_ids JSON        NOT NULL COMMENT '알림 대상 회원 ID 목록',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '발송 시각',
    INDEX idx_fee_notifications_club_month (club_id, year, month)
) ENGINE=InnoDB COLLATE=utf8mb4_unicode_ci COMMENT='회비 미납 알림 이력';
