CREATE TABLE domain_event_inbox (
    event_id      VARCHAR(26)  NOT NULL,
    consumer_name VARCHAR(100) NOT NULL,
    processed_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (event_id, consumer_name)
);
