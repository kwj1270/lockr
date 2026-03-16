# DOMAIN CONTEXT

도메인 구조와 비즈니스 로직 이해를 위한 컨텍스트 문서:

- `docs/domains/README.md` - 전체 도메인 구조 개요
- `docs/domains/auth-context.md` - 인증 도메인 (Admin, OIDC, SignIn, SignUp)
- `docs/domains/users-context.md` - 사용자 도메인
- `docs/domains/club-context.md` - 클럽 도메인 (핵심: Club, Schedule, Chat, Feed, Recruitment)
- `docs/domains/notification-context.md` - 알림 도메인
- `docs/domains/home-context.md` - 홈 도메인

새 기능 구현 전 관련 도메인 컨텍스트 문서를 먼저 읽어 비즈니스 규칙과 도메인 관계를 파악할 것.

# CUSTOM COMMANDS

프로젝트 전용 슬래시 명령어 (`.claude/commands/`):

| 명령어 | 설명 | 예시 |
|--------|------|------|
| `/go` | TDD Plan의 다음 테스트 진행 | `/go docs/plans/schedule-domain-plan.md` |
| `/test` | 테스트 실행 | `/test schedule`, `/test ScheduleTest` |
| `/context` | 도메인 컨텍스트 조회 | `/context club`, `/context all` |
| `/new-plan` | 새 TDD Plan 파일 생성 | `/new-plan user-profile` |
| `/build` | 프로젝트 빌드 | `/build`, `/build fast`, `/build clean` |
| `/db` | DB 작업 | `/db up`, `/db migrate`, `/db status` |
| `/api` | API 분석/생성 | `/api list club`, `/api new POST /api/v1/...` |
| `/research` | 도메인/기술/프로젝트 리서치 | `/research 축구 동호회 회비 관리` |
| `/spec` | 새 기능 PRD + TDD Plan 생성 | `/spec 클럽 회비 관리` |

# PARALLEL WORK (병렬 작업)

## 병렬 실행 원칙

1. **독립적인 작업은 항상 병렬로 요청**
   - 여러 파일 읽기/검색이 필요할 때
   - 여러 Agent 탐색이 필요할 때
   - 독립적인 빌드/테스트 실행

2. **의존성 있는 작업은 순차 실행**
   - 파일 읽기 → 수정 → 테스트 (순차)
   - API 호출 결과에 따른 다음 작업 (순차)

# PLAN FILES

Plan 파일들은 `docs/plans/` 폴더에 기능별로 분리되어 있습니다:

- `docs/plans/schedule-domain-plan.md` - Schedule 도메인 TDD
- `docs/plans/admin-signin-plan.md` - 관리자 로그인 기능
- `docs/plans/user-withdraw-plan.md` - 회원 탈퇴 기능
- `docs/plans/home-card-plan.md` - Home Card 기능
- `docs/plans/notification-plan.md` - Notification 도메인

When I say "go" with a specific plan file, find the next unmarked test in that plan, implement the test, then implement only enough code to make that test pass.

예시: `go docs/plans/schedule-domain-plan.md`

# COMMON COMMANDS

## 빌드
- `./gradlew build` - 전체 빌드
- `./gradlew build -x test` - 테스트 제외 빌드
- `./gradlew clean build` - 클린 빌드

## 테스트
- `./gradlew test` - 전체 테스트
- `./gradlew test --tests "ScheduleTest"` - 특정 테스트 클래스
- `./gradlew test --tests "*ScheduleTest.shouldCreate*"` - 특정 테스트 메서드

## 데이터베이스
- `docker-compose up -d mysql redis` - 로컬 DB 실행
- `./gradlew flywayMigrate` - 마이그레이션 실행
- `mysql -h 127.0.0.1 -u root -p1234 lockr < infra/mysql/seed/seed_data.sql` - Seed 데이터 로드

## Seed 데이터 (로컬 개발용)
- 위치: `infra/mysql/seed/seed_data.sql`
- 관리자: admin / test1234
- 사용자: 80명 (각 클럽당 20명)
- 클럽 4개:
  - FC 강남 (강남구) - 회장1, 매니저2, 코치1, 일반16
  - 마포 FC (마포구) - 회장1, 매니저1, 코치2, 일반16
  - 송파 유나이티드 (송파구) - 회장1, 매니저1, 코치1, 일반17
  - 영등포 FC (영등포구) - 회장1, 매니저2, 코치1, 일반16

## JOOQ
- `./gradlew generateJooq` - JOOQ 코드 생성
