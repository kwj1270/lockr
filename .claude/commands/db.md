데이터베이스 관련 작업을 수행합니다.

명령: $ARGUMENTS

## 사용 가능한 명령
- `up`: Docker로 MySQL, Redis 실행 (`docker-compose up -d mysql redis`)
- `down`: Docker 컨테이너 종료 (`docker-compose down`)
- `migrate`: Flyway 마이그레이션 실행 (`./gradlew flywayMigrate`)
- `status`: 마이그레이션 상태 확인 (`./gradlew flywayInfo`)
- `seed`: 테스트용 기본 데이터 로드 (`mysql -h 127.0.0.1 -u root -p1234 lockr < infra/mysql/seed/seed_data.sql`)
- `reset`: DB 초기화 후 마이그레이션 + seed 실행

## Seed 데이터 내용 (`infra/mysql/seed/seed_data.sql`)
- 테스트 사용자 5명 (김철수, 이영희, 박민수, 정수진, 최동현)
- 관리자 계정: admin / test1234
- 테스트 클럽 2개 (FC 테스트, 풋살러스)
- 클럽 멤버 배치 (회장, 매니저, 코치, 일반)

## 주의사항
- 마이그레이션 파일은 `src/main/resources/db/migration/` 에 위치
- 파일명 규칙: `V{버전}__description.sql`
- 로컬 DB 연결 정보는 `application-local.yml` 참조
- seed 실행 전 migrate 먼저 실행 필요