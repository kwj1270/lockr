---
paths:
  - "src/main/resources/db/migration/**/*.sql"
  - "infra/mysql/**/*.sql"
---

# DB Migration Rules (Flyway)

## 파일 네이밍
- `V{번호}__{설명}.sql` 형식 (언더스코어 2개)
- 번호는 기존 마이그레이션의 다음 순번 사용
- 설명은 snake_case: `V12__create_fee_tables.sql`

## 테이블 규칙
- PK: `id VARCHAR(26)` (ULID) + `PRIMARY KEY`
- 시간 필드: `DATETIME(6)` + `DEFAULT CURRENT_TIMESTAMP(6)`
- Soft delete: `deleted_at DATETIME(6) NULL`
- 모든 테이블에 `created_at`, `updated_at`, `deleted_at` 포함
- COLLATE: `utf8mb4_unicode_ci`
- 테이블/컬럼에 `COMMENT` 필수

## 컬럼 네이밍
- snake_case 사용
- FK: `{참조테이블_단수}_id` (예: `club_id`, `user_id`, `schedule_id`)
- 상태: `status VARCHAR(50)` + COMMENT에 가능한 값 명시
- 타입: `type VARCHAR(50)` + COMMENT에 가능한 값 명시

## 인덱스
- 네이밍: `idx_{테이블}_{컬럼}` (예: `idx_schedules_club_id`)
- FK 컬럼에는 반드시 인덱스 생성
- 자주 조회되는 조합은 복합 인덱스 생성
- UNIQUE 제약: `uk_{테이블}_{컬럼들}` (예: `uk_attendances_schedule_user`)

## 기존 마이그레이션 참고
- V1: 초기 테이블 (users, sign_in)
- V2~V11: 도메인별 테이블 (clubs, recruitment, squad, lineup, chat, feed, schedule, notification, stats, shorts)
