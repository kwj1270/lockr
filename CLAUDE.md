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
| `/test` | 테스트 실행 | `/test schedule`, `/test ScheduleTest` |
| `/context` | 도메인 컨텍스트 조회 | `/context club`, `/context all` |
| `/new-plan` | 새 TDD Plan 파일 생성 | `/new-plan user-profile` |
| `/build` | 프로젝트 빌드 | `/build`, `/build fast`, `/build clean` |
| `/db` | DB 작업 | `/db up`, `/db migrate`, `/db status` |
| `/api` | API 분석/생성 | `/api list club`, `/api new POST /api/v1/...` |
| `/research` | 도메인/기술/프로젝트 리서치 | `/research 축구 동호회 회비 관리` |
| `/spec` | 새 기능 PRD + TDD Plan 생성 | `/spec 클럽 회비 관리` |

# Seed 데이터 (로컬 개발용)

- 위치: `infra/mysql/seed/seed_data.sql`
- 관리자: admin / test1234
- 사용자: 80명 (각 클럽당 20명)
- 클럽 4개:
  - FC 강남 (강남구) - 회장1, 매니저2, 코치1, 일반16
  - 마포 FC (마포구) - 회장1, 매니저1, 코치2, 일반16
  - 송파 유나이티드 (송파구) - 회장1, 매니저1, 코치1, 일반17
  - 영등포 FC (영등포구) - 회장1, 매니저2, 코치1, 일반16