CREATE TABLE domain_event_outbox (
    seq          BIGINT       NOT NULL AUTO_INCREMENT,
    event_id     VARCHAR(26)  NOT NULL,
    event_type   VARCHAR(200) NOT NULL,
    source       VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(26)  NOT NULL,
    occurred_at  DATETIME(6)  NOT NULL,
    payload      JSON         NOT NULL,
    processed    TINYINT(1)   NOT NULL DEFAULT 0,
    retry_count  INT          NOT NULL DEFAULT 0,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NULL,
    PRIMARY KEY (seq),
    UNIQUE KEY uq_event_id (event_id),
    INDEX idx_unprocessed (processed, seq),
    INDEX idx_event_type_time (event_type, occurred_at)
);
