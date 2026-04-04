-- ============================================================
-- V13: 회비 정책 및 납부 기록 테이블
-- ============================================================

CREATE TABLE fee_policies
(
    id             CHAR(26)     NOT NULL COMMENT '회비 정책 식별키 (ULID)' PRIMARY KEY,
    club_id        CHAR(26)     NOT NULL COMMENT '클럽 식별키',
    amount         INT          NOT NULL DEFAULT 0 COMMENT '월 회비 금액',
    due_day        INT          NOT NULL DEFAULT 15 COMMENT '납부 기한 (1~28일)',
    bank_name      VARCHAR(50)  NULL COMMENT '입금 은행명',
    account_number VARCHAR(50)  NULL COMMENT '계좌번호',
    account_holder VARCHAR(50)  NULL COMMENT '예금주',
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    UNIQUE KEY uk_fee_policies_club (club_id)
) ENGINE = InnoDB
  COLLATE = utf8mb4_unicode_ci COMMENT = '클럽 회비 정책';

CREATE TABLE fee_records
(
    id         CHAR(26)     NOT NULL COMMENT '납부 기록 식별키 (ULID)' PRIMARY KEY,
    club_id    CHAR(26)     NOT NULL COMMENT '클럽 식별키',
    member_id  CHAR(26)     NOT NULL COMMENT '회원 식별키',
    year       INT          NOT NULL COMMENT '납부 연도',
    month      INT          NOT NULL COMMENT '납부 월',
    status     VARCHAR(10)  NOT NULL DEFAULT 'UNPAID' COMMENT '납부 상태 (UNPAID/PAID)',
    memo       VARCHAR(500) NULL COMMENT '메모',
    updated_by CHAR(26)     NULL COMMENT '최종 수정자 식별키',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    UNIQUE KEY uk_fee_records_member_month (club_id, member_id, year, month)
) ENGINE = InnoDB
  COLLATE = utf8mb4_unicode_ci COMMENT = '회비 납부 기록';

CREATE INDEX idx_fee_records_club_month ON fee_records (club_id, year, month);
