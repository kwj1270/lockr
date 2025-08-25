-- auto-generated definition
create table http_logs
(
    id          varchar(128) not null comment '고유한 로그 ID (UUID)'
        primary key,
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
)
    collate = utf8mb4_unicode_ci;

create index idx_child_guid
    on http_logs (child_guid);

create index idx_client_ip
    on http_logs (client_ip);

create index idx_root_guid
    on http_logs (root_guid);

create index idx_tx_date
    on http_logs (tx_date);

create index idx_tx_time
    on http_logs (tx_time);

create index idx_user_id
    on http_logs (user_id);

