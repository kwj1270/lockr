ALTER TABLE fee_records
    ADD COLUMN paid_at DATETIME(6) NULL COMMENT '납부 처리 시각' AFTER status;
