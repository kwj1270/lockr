---
globs:
  - "src/main/resources/db/migration/**/*.sql"
description: "Flyway 마이그레이션, DB 스키마, SQL, 테이블 생성, 컬럼 추가"
---

## Flyway 마이그레이션 규칙

- **네이밍**: `V{번호}__{설명}.sql` (V + 숫자 + 더블 언더스코어 + snake_case 설명)
- **현재 최신**: V14 — 새 마이그레이션은 V15부터
- **롤백 불가**: Flyway는 롤백을 지원하지 않음. 수정 시 새 마이그레이션 추가
- **jOOQ 연동**: 마이그레이션 추가 후 `./gradlew generateJooqClasses` 실행 필요
- **타입 매핑**: `int unsigned → Long`, `tinyint(1) → Boolean` (jOOQ forced type)
- **ID 컬럼**: `VARCHAR(26)` (ULID), NOT NULL, PRIMARY KEY
- **Soft delete**: `deleted_at DATETIME(6) NULL` 패턴
- **타임스탬프**: `created_at DATETIME(6) NOT NULL`, `updated_at DATETIME(6) NULL`
