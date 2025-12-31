-- recruitments definition
use lockr;
CREATE TABLE `recruitments`
(
    `id`               CHAR(128)    NOT NULL COMMENT '모집 공고 식별키' primary key,
    `club_id`          CHAR(128)    NOT NULL COMMENT '클럽 식별키',
    `title`            VARCHAR(200) NOT NULL COMMENT '모집 제목',
    `content`          TEXT         NOT NULL COMMENT '모집 내용',
    `status`           VARCHAR(50)  NOT NULL COMMENT '모집 상태 (RECRUITING, CLOSED)',
    `is_public`        BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '공개 여부',
    `recruitment_type` VARCHAR(10)  NOT NULL DEFAULT 'FULL' COMMENT '모집 공고 유형(SIMPLE, FULL)',
    `activity_city`  VARCHAR(100) NOT NULL COMMENT '주요 활동 지역',
    `activity_district`  VARCHAR(100) NOT NULL COMMENT '주요 활동 지역',
    `activity_days`    VARCHAR(100) NOT NULL COMMENT '주요 활동 요일(요일 , 로 구분)',
    `activity_time`    VARCHAR(100) NOT NULL COMMENT '주요 활동 시간',
    `monthly_fee`      INT          NOT NULL DEFAULT 0 COMMENT '월 회비 (원)',
    `contact_method`   TEXT         NOT NULL COMMENT '문의 방법',
    `created_at`       DATETIME(6)  NOT NULL COMMENT '생성 시각',
    `updated_at`       DATETIME(6)  NOT NULL COMMENT '변경 시각',
    `deleted_at`       DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '클럽 모집 공고';

-- Indexes
CREATE INDEX `idx_recruitments_public_status` ON `recruitments` (`is_public`, `status`, `deleted_at`);
CREATE INDEX `idx_recruitments_club` ON `recruitments` (`club_id`, `status`, `deleted_at`);

-- table for storing user application resume to clubs
CREATE TABLE `applications`
(
    `id`                      CHAR(128)    NOT NULL PRIMARY KEY COMMENT '트라이아웃 식별키',
    `recruitment_id`          CHAR(128)    NOT NULL COMMENT '모집 공고 식별키',
    `club_id`                 CHAR(128)    NOT NULL COMMENT '클럽 식별키',
    `applicant_user_id`       CHAR(128)    NOT NULL COMMENT '지원자 식별키',
    `application_type`        VARCHAR(10)  NOT NULL DEFAULT 'FULL' COMMENT '지원서 유형 (SIMPLE, FULL)',
    `status`                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '지원 상태 (PENDING, APPROVED, REJECTED, WITHDRAWN)',
    `processed_by_user_id`    CHAR(128)    NULL COMMENT '처리자 식별키',
    `processed_at`            DATETIME(6)  NULL COMMENT '처리 시각',
    `reject_reason`           TEXT         NULL COMMENT '거절 사유',
    `name`                    VARCHAR(100) NOT NULL COMMENT '지원자 이름',
    `phone`                   VARCHAR(20)  NOT NULL COMMENT '지원자 연락처',
    `email`                   VARCHAR(100) NOT NULL COMMENT '지원자 이메일',
    `birth_date`              CHAR(8)      NOT NULL COMMENT '생년월일(YYYYMMDD)',
    `gender`                  VARCHAR(10)  NOT NULL COMMENT '성별',
    `emergency_contact_phone` VARCHAR(20)  NULL COMMENT '비상 연락처',
    `profile_image_url`       VARCHAR(500) NULL COMMENT '프로필 이미지 URL',
    `nationality`             VARCHAR(50)  NULL COMMENT '국적',
    `address`                 VARCHAR(200) NULL COMMENT '주소',
    `height`                  SMALLINT     NULL COMMENT '키(cm)',
    `weight`                  SMALLINT     NULL COMMENT '몸무게(kg)',
    `introduction`            TEXT         NOT NULL COMMENT '자기소개',
    `sport_type`              VARCHAR(200) NOT NULL COMMENT '스포츠 타입',
    `sport_specific_fields`   JSON         NOT NULL COMMENT '스포츠별 맞춤 정보',
    `created_at`              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at`              TIMESTAMP    NULL
) COLLATE = utf8mb4_unicode_ci COMMENT = '클럽 입단 트라이아웃 지원서';
-- Indexes
CREATE INDEX `idx_recruitment` ON `applications` (`recruitment_id`, `status`, `deleted_at`);
CREATE INDEX `idx_club` ON `applications` (`club_id`, `status`, `deleted_at`);
CREATE INDEX `idx_applicant` ON `applications` (`applicant_user_id`, `deleted_at`);
CREATE INDEX `idx_status` ON `applications` (`status`, `created_at`);

