-- ============================================================
-- V1: 기본 테이블 (http_log, users, auth, admin)
-- ============================================================

-- HTTP 요청 로그
CREATE TABLE http_log
(
    id          CHAR(26)     NOT NULL COMMENT '고유한 로그 ID (UUID)' PRIMARY KEY,
    root_guid   VARCHAR(128) NOT NULL COMMENT '루트 GUID (ULID)',
    child_guid  VARCHAR(128) NOT NULL COMMENT '자식 GUID (ULID)',
    tx_date     VARCHAR(24)  NOT NULL COMMENT '로그 기록 날짜',
    tx_time     VARCHAR(24)  NULL COMMENT '로그 기록 시간',
    client_ip   VARCHAR(45)  NOT NULL COMMENT '요청을 보낸 클라이언트의 IP 주소',
    user_id     VARCHAR(128) NULL COMMENT '요청 보낸 사용자의 고유 ID',
    http_method VARCHAR(10)  NOT NULL COMMENT 'HTTP 요청 메소드 (GET, POST 등)',
    path        VARCHAR(255) NOT NULL COMMENT '요청된 경로',
    status_code SMALLINT     NOT NULL COMMENT 'HTTP 응답 상태 코드',
    headers     JSON         NULL COMMENT '요청 헤더 정보',
    body        JSON         NULL COMMENT '요청 본문'
) COLLATE = utf8mb4_unicode_ci COMMENT = 'HTTP 요청 로그';

CREATE INDEX idx_http_log_root_guid ON http_log (root_guid);
CREATE INDEX idx_http_log_child_guid ON http_log (child_guid);
CREATE INDEX idx_http_log_tx_date ON http_log (tx_date);
CREATE INDEX idx_http_log_tx_time ON http_log (tx_time);
CREATE INDEX idx_http_log_client_ip ON http_log (client_ip);
CREATE INDEX idx_http_log_user_id ON http_log (user_id);

-- 사용자 기본 정보(불변)
CREATE TABLE users
(
    id         CHAR(26)    NOT NULL COMMENT '사용자 식별키 (ULID)' PRIMARY KEY,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '사용자 기본 정보';

-- 사용자 추가 정보(가변)
CREATE TABLE user_additional_info
(
    id            CHAR(26)     NOT NULL COMMENT '사용자 추가 정보 식별키 (ULID)' PRIMARY KEY,
    user_id       CHAR(26)     NOT NULL COMMENT '사용자 ID',
    name          VARCHAR(100) NULL COMMENT '실명',
    birth_date    CHAR(8)      NULL COMMENT '생년월일(YYYYMMDD)',
    phone         VARCHAR(20)  NULL COMMENT '휴대폰 번호',
    gender        CHAR(1)      NULL COMMENT '성별 (M/F)',
    profile_image VARCHAR(500) NULL COMMENT '프로필 이미지 URL',
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at    DATETIME(6)  NULL COMMENT '삭제 시각',
    UNIQUE KEY uk_user_additional_info_user_id (user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT = '사용자 추가 정보';

-- CI/DI
CREATE TABLE ci_di
(
    id            CHAR(26)     NOT NULL COMMENT 'CI/DI 식별키 (ULID)' PRIMARY KEY,
    user_id       CHAR(26)     NULL COMMENT '사용자 ID',
    ci            VARCHAR(255) NOT NULL COMMENT 'CI 값',
    di            VARCHAR(255) NULL COMMENT 'DI 값',
    metadata      JSON         NULL COMMENT '추가 정보',
    auth_method   VARCHAR(50)  NOT NULL COMMENT '인증 방법 (PHONE, CERTIFICATE, FINANCIAL_CERT)',
    auth_provider VARCHAR(50)  NOT NULL COMMENT '인증 제공사 (PASS, NICE, 금융결제원)',
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at    DATETIME(6)  NULL COMMENT '삭제 시각',
    UNIQUE KEY uk_ci_di_ci (ci)
) COLLATE = utf8mb4_unicode_ci COMMENT = 'CI/DI 본인인증';

CREATE INDEX idx_ci_di_user_id ON ci_di (user_id);
CREATE INDEX idx_ci_di_ci ON ci_di (ci);
CREATE INDEX idx_ci_di_di_provider ON ci_di (di, auth_provider);

-- OIDC 소셜 로그인
CREATE TABLE oidc
(
    id         CHAR(26)     NOT NULL COMMENT '식별자 ID (ULID)' PRIMARY KEY,
    user_id    CHAR(26)     NULL COMMENT '사용자 ID',
    provider   VARCHAR(50)  NOT NULL COMMENT '제공자 (APPLE, GOOGLE)',
    identifier VARCHAR(255) NOT NULL COMMENT '식별자',
    metadata   TEXT         NULL COMMENT '추가 정보',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at DATETIME(6)  NULL COMMENT '삭제 시각',
    UNIQUE KEY uk_oidc_identity (provider, identifier)
) COLLATE = utf8mb4_unicode_ci COMMENT = 'OIDC 소셜 로그인';

CREATE INDEX idx_oidc_user_id ON oidc (user_id);

-- 관리자
CREATE TABLE admin
(
    id         VARCHAR(100) NOT NULL COMMENT '관리자 ID' PRIMARY KEY,
    password   VARCHAR(255) NOT NULL COMMENT '비밀번호',
    user_id    CHAR(26)     NULL COMMENT '사용자 ID',
    role       VARCHAR(50)  NOT NULL COMMENT '권한 (BASIC 등)',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '관리자';

-- 로그인 기록
CREATE TABLE sign_in
(
    id          CHAR(26)     NOT NULL COMMENT '로그인 고유 ID (ULID)' PRIMARY KEY,
    user_id     CHAR(26)     NOT NULL COMMENT '사용자 고유 ID (ULID)',
    device_id   VARCHAR(255) NOT NULL COMMENT '기기의 ID',
    device_name VARCHAR(255) NOT NULL COMMENT '기기 이름',
    device_os   VARCHAR(50)  NOT NULL COMMENT '기기 OS',
    ip_address  VARCHAR(45)  NULL COMMENT '로그인한 IP 주소',
    user_agent  VARCHAR(512) NULL COMMENT '사용자 에이전트',
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '로그인 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '로그인 기록';

CREATE INDEX idx_sign_in_user_id ON sign_in (user_id);

-- 로그인 토큰
CREATE TABLE sign_in_tokens
(
    id         CHAR(26)     NOT NULL COMMENT '토큰 고유 ID (ULID)' PRIMARY KEY,
    user_id    CHAR(26)     NOT NULL COMMENT '사용자 고유 ID (ULID)',
    sign_in_id CHAR(26)     NOT NULL COMMENT '로그인 고유 ID (ULID)',
    token      VARCHAR(255) NOT NULL COMMENT '자동 로그인 토큰',
    expires_at DATETIME(6)  NOT NULL COMMENT '만료 시각',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    deleted_at DATETIME(6)  NULL COMMENT '토큰 무효화 시각',
    UNIQUE KEY uk_sign_in_tokens_token (token)
) COLLATE = utf8mb4_unicode_ci COMMENT = '로그인 토큰';

CREATE INDEX idx_sign_in_tokens_token ON sign_in_tokens (token);
CREATE INDEX idx_sign_in_tokens_expires_at ON sign_in_tokens (expires_at);
