-- auto-generated definition
use lockr;
CREATE TABLE http_log
(
    id          CHAR(26) not null comment '고유한 로그 ID (UUID)' primary key,
    root_guid   varchar(128) not null comment '루트 GUID (ULID)',
    child_guid  varchar(128) not null comment '자식 GUID (ULID)',
    tx_date     varchar(24)  not null comment '로그 기록 날짜',
    tx_time     varchar(24)  null comment '로그 기록 시간',
    client_ip   varchar(45)  not null comment '요청을 보낸 클라이언트의 IP 주소',
    user_id     varchar(128) null comment '요청 보낸 사용자의 고유 ID (인증되지 않은 경우 NULL)',
    http_method varchar(10)  not null comment 'HTTP 요청 메소드 (GET, POST 등)',
    path        varchar(255) not null comment '요청된 경로',
    status_code smallint     not null comment 'HTTP 응답 상태 코드 (200, 404, 500 등)',
    headers     json         null comment '요청 헤더 정보',
    body        json         null comment '요청 본문 (민감 정보 마스킹 필요)'
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_http_log_child_guid ON http_log (child_guid);

CREATE INDEX idx_http_log_client_ip ON http_log (client_ip);

CREATE INDEX idx_http_log_root_guid ON http_log (root_guid);

CREATE INDEX idx_http_log_tx_date ON http_log (tx_date);

CREATE INDEX idx_http_log_tx_time ON http_log (tx_time);

CREATE INDEX idx_http_log_user_id ON http_log (user_id);
