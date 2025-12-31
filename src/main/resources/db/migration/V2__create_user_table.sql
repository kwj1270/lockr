-- 사용자 기본 정보(불변)
USE lockr;

CREATE TABLE `users`
(
    `id`         CHAR(26)    NOT NULL COMMENT '사용자 식별키 (ULID)' PRIMARY KEY,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    `deleted_at` DATETIME(6) NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci;

-- 사용자 추가 정보(가변)
CREATE TABLE `user_additional_info`
(
    `id`         CHAR(26)     NOT NULL COMMENT '사용자 추가 정보 식별키 (ULID)' PRIMARY KEY,
    `user_id`    CHAR(26)     NOT NULL COMMENT '사용자 ID',
    `name`       VARCHAR(100) NULL COMMENT '실명',
    `birth_date`  CHAR(8)      NULL COMMENT '생년월일(YYYYMMDD)',
    `phone`      VARCHAR(20)  NULL COMMENT '휴대폰 번호',
    `gender`     CHAR(1)      NULL COMMENT '성별 (M/F)',
    `created_at` DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    `updated_at` DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    `deleted_at` DATETIME(6)  NULL COMMENT '삭제 시각',
    UNIQUE KEY `uk_user_id` (`user_id`)
) COLLATE = utf8mb4_unicode_ci;

-- CI/DI 테이블
CREATE TABLE ci_di
(
    `id`            CHAR(26)     NOT NULL COMMENT 'CI/DI 식별키 (ULID)' PRIMARY KEY,
    `user_id`       CHAR(26)     NULL COMMENT '사용자 ID',
    `ci`            VARCHAR(255) NOT NULL COMMENT 'CI 값',
    `di`            VARCHAR(255) NULL COMMENT 'DI 값',
    `metadata`      JSON         NULL COMMENT '추가 정보(CI/DI 외 수집된 정보)',
    `auth_method`   VARCHAR(50)  NOT NULL COMMENT '인증 방법(PHONE, CERTIFICATE, FINANCIAL_CERT)',
    `auth_provider` VARCHAR(50)  NOT NULL COMMENT '인증 제공사(PASS, NICE, 금융결제원)',
    `created_at`    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    `updated_at`    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    `deleted_at`    DATETIME(6)  NULL COMMENT '삭제 시각',
    UNIQUE KEY `uk_ci` (`ci`),
    INDEX `idx_ci_di_verifications_user_id` (`user_id`),
    INDEX `idx_ci_di_verifications_ci` (`ci`),
    INDEX `idx_ci_di_verifications_di` (`di`, `auth_provider`)
) COLLATE = utf8mb4_unicode_ci;

-- OIDC 테이블
CREATE TABLE `oidc`
(
    `id`          CHAR(26)     NOT NULL COMMENT '식별자 ID (ULID)' PRIMARY KEY,
    `user_id`     CHAR(26)     NULL COMMENT '사용자 ID',
    `provider`    VARCHAR(50)  NOT NULL COMMENT '제공자(APPLE,GOOGLE)',
    `identifier`  VARCHAR(255) NOT NULL COMMENT '식별자',
    `metadata`    TEXT         NULL COMMENT '추가 정보',
    `created_at`  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    `updated_at`  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    `deleted_at`  DATETIME(6)  NULL COMMENT '삭제 시각',
    UNIQUE KEY `uk_identity` (`provider`, `identifier`),
    INDEX `idx_user_identities_user_id` (`user_id`)
) COLLATE = utf8mb4_unicode_ci;

-- 관리자 테이블
CREATE TABLE `admin`
(
    `id`         VARCHAR(100) NOT NULL COMMENT '관리자 ID' PRIMARY KEY,
    `password`   VARCHAR(255) NOT NULL COMMENT '비밀번호 (테스트용 - 암호화 없음)',
    `user_id`     CHAR(26)    NULL COMMENT '사용자 ID',
    `role`       VARCHAR(50)  NOT NULL COMMENT '권한 (BASIC 등)',
    `created_at` DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    `updated_at` DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    `deleted_at` DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci;
