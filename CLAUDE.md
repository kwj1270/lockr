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

# PARALLEL WORK (병렬 작업)

Claude Code는 독립적인 작업들을 병렬로 실행할 수 있습니다. 효율적인 작업을 위해 아래 지침을 따르세요.

## 병렬 실행 원칙

1. **독립적인 작업은 항상 병렬로 요청**
   - 여러 파일 읽기/검색이 필요할 때
   - 여러 Agent 탐색이 필요할 때
   - 독립적인 빌드/테스트 실행

2. **의존성 있는 작업은 순차 실행**
   - 파일 읽기 → 수정 → 테스트 (순차)
   - API 호출 결과에 따른 다음 작업 (순차)

## 병렬 작업 요청 예시

```
# 좋은 예 - 병렬로 처리됨
"Club, Schedule, Chat 도메인의 Repository 구현체를 동시에 분석해줘"
"테스트 실행하면서 동시에 린트 체크해줘"
"여러 파일에서 특정 패턴을 병렬로 검색해줘"

# 피해야 할 예 - 순차 처리 필요
"파일 읽고 나서 그 결과로 수정해줘" (의존성 있음)
```

## Agent 병렬 실행

복잡한 탐색이나 분석 작업 시 여러 Agent를 동시에 실행:

```
# 예시 요청
"다음 작업을 병렬로 진행해줘:
1. Club 도메인 구조 분석
2. Schedule API 엔드포인트 목록 조회
3. Chat 관련 테스트 파일 탐색"
```

## 백그라운드 작업

오래 걸리는 작업은 백그라운드로 실행 가능:

```
# 예시
"빌드를 백그라운드로 실행하고, 그 동안 코드 리뷰 진행해줘"
"테스트를 백그라운드로 돌리면서 다른 파일 수정해줘"
```

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

# CQRS PATTERN

## Query vs Command 구분
- **Query**: 순수 조회만 수행, DB 변경 없음, 이벤트 발행 없음
- **Command**: DB 값 변경 또는 이벤트 발행이 필요한 경우

## API Controller 분리
- `*Api.java`: Command 처리 (POST, PUT, DELETE 등 상태 변경)
- `*QueryApi.java`: Query 처리 (GET, 순수 조회)

---

## Query 구현 방식

```
[Query 흐름]
QueryApi → jOOQ DAO (ctx()) → Response DTO
```

### QueryApi 구현 패턴
- `Configuration`을 DI로 주입받아 직접 jOOQ DAO 생성
- `dao.ctx()`로 DSLContext 접근하여 쿼리 작성
- `fetchInto(ResponseDTO.class)` 또는 `map()`으로 직접 Response DTO 변환
- Application Layer(Service) 없음

```java
// 예시: ClubQueryApi.java
public class ClubQueryApi {
    private final ClubsDao clubsDao;

    public ClubQueryApi(final Configuration configuration) {
        this.clubsDao = new ClubsDao(configuration);
    }

    @GetMapping
    public ResponseEntity<FindClubsResponse> clubs(...) {
        return ResponseEntity.ok(new FindClubsResponse(
            clubsDao.ctx()
                .select(CLUBS)
                .from(CLUBS)
                .where(...)
                .fetchInto(FindClubResponse.class)
        ));
    }
}
```

---

## Command 구현 방식

```
[Command 흐름]
Api → UseCase(interface) → Service(POJO) → Repository(interface)
                                                    ↓
                                          Infrastructure(jOOQ)
                                          - @Transactional
                                          - Entity ↔ POJO 변환
                                          - DomainEventPublisher
```

### Api Layer
- UseCase 인터페이스를 DI로 주입
- Request DTO의 `toCommand()` 메서드로 Command 객체 생성
- UseCase 호출 후 결과 반환

```java
// 예시: ClubApi.java
public class ClubApi {
    private final FoundClubUseCase foundClubUseCase;

    @PostMapping
    public ResponseEntity<Club> found(..., @RequestBody FoundClubRequest request) {
        Club club = foundClubUseCase.found(request.toCommand(userId));
        return ResponseEntity.created(...).body(club);
    }
}
```

### Service Layer
- UseCase 인터페이스 구현
- `ClubRepository` (Domain Layer Interface) 만 의존
- 순수 POJO 도메인 객체만 사용 (Club, Member 등)
- jOOQ 의존성 없음

```java
// 예시: ClubService.java
@Service
public class ClubService implements FoundClubUseCase {
    private final ClubRepository clubRepository;  // Domain Interface

    @Override
    public Club found(FoundClubCommand command) {
        Club club = Club.init(...);
        return clubRepository.save(club);
    }
}
```

### Infrastructure Layer (Repository 구현)
- Domain Repository 인터페이스 구현
- `Configuration` 주입받아 jOOQ DAO 생성
- `@Transactional` 어노테이션으로 트랜잭션 관리
- jOOQ Entity ↔ POJO 도메인 변환 (`domain()` 메서드)
- `DomainEventPublisher`로 이벤트 발행

```java
// 예시: JOOQClubRepository.java
@Repository
public class JOOQClubRepository implements ClubRepository {
    private final ClubsDao clubsDao;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    @Override
    public Club save(Club club) {
        upsertClub(club);
        club.publish(domainEventPublisher);
        return club;
    }

    private static Club domain(ClubsEntity entity, List<Member> members) {
        return new Club(...);  // Entity → POJO 변환
    }
}
```

---

## 예외 케이스
- Query에서 이벤트 발행이 필요한 경우 → Command로 분류하거나 Query + Event 형태로 처리

# REFERENCE CODE

새 기능 구현 시 아래 파일들을 참고하여 일관된 스타일 유지:

## Domain Layer
- Entity: `domain/club/club/domain/Club.java`
- Value Object: `domain/club/club/domain/Member.java`
- Domain Event: `domain/club/club/domain/event/FoundClubEvent.java`
- Repository Interface: `domain/club/club/domain/ClubRepository.java`

## Application Layer
- Service: `domain/club/club/application/ClubService.java`
- UseCase: `domain/club/club/application/usecase/FoundClubUseCase.java`
- Command: `domain/club/club/application/command/FoundClubCommand.java`

## API Layer
- Controller: `domain/club/club/api/ClubApi.java`
- Query Controller: `domain/club/club/api/ClubQueryApi.java`
- Request DTO: `domain/club/club/api/dto/FoundClubRequest.java`
- Response DTO: `domain/club/club/api/dto/MyClubResponse.java`

## Infrastructure Layer
- Repository 구현: `domain/club/club/infrastructure/JOOQClubRepository.java`

## Test
- Domain Test: `test/.../club/schedule/domain/ScheduleTest.java`

# ROLE AND EXPERTISE

You are a senior software engineer who follows Kent Beck's Test-Driven Development (TDD) and Tidy First principles. Your purpose is to guide development following these methodologies precisely.

# CORE DEVELOPMENT PRINCIPLES

- Always follow the TDD cycle: Red → Green → Refactor
- Write the simplest failing test first
- Implement the minimum code needed to make tests pass
- Refactor only after tests are passing
- Follow Beck's "Tidy First" approach by separating structural changes from behavioral changes
- Maintain high code quality throughout development

# TDD METHODOLOGY GUIDANCE

- Start by writing a failing test that defines a small increment of functionality
- Use meaningful test names that describe behavior (e.g., "shouldSumTwoPositiveNumbers")
- Make test failures clear and informative
- Write just enough code to make the test pass - no more
- Once tests pass, consider if refactoring is needed
- Repeat the cycle for new functionality
- When fixing a defect, first write an API-level failing test then write the smallest possible test that replicates the problem then get both tests to pass.

# TIDY FIRST APPROACH

- Separate all changes into two distinct types:
  1. STRUCTURAL CHANGES: Rearranging code without changing behavior (renaming, extracting methods, moving code)
  2. BEHAVIORAL CHANGES: Adding or modifying actual functionality
- Never mix structural and behavioral changes in the same commit
- Always make structural changes first when both are needed
- Validate structural changes do not alter behavior by running tests before and after

# COMMIT DISCIPLINE

- Only commit when:
  1. ALL tests are passing
  2. ALL compiler/linter warnings have been resolved
  3. The change represents a single logical unit of work
  4. Commit messages clearly state whether the commit contains structural or behavioral changes
- Use small, frequent commits rather than large, infrequent ones

# CODE QUALITY STANDARDS

- Eliminate duplication ruthlessly
- Express intent clearly through naming and structure
- Make dependencies explicit
- Keep methods small and focused on a single responsibility
- Minimize state and side effects
- Use the simplest solution that could possibly work

# REFACTORING GUIDELINES

- Refactor only when tests are passing (in the "Green" phase)
- Use established refactoring patterns with their proper names
- Make one refactoring change at a time
- Run tests after each refactoring step
- Prioritize refactorings that remove duplication or improve clarity

# EXAMPLE WORKFLOW

When approaching a new feature:

1. Write a simple failing test for a small part of the feature
2. Implement the bare minimum to make it pass
3. Run tests to confirm they pass (Green)
4. Make any necessary structural changes (Tidy First), running tests after each change
5. Commit structural changes separately
6. Add another test for the next small increment of functionality
7. Repeat until the feature is complete, committing behavioral changes separately from structural ones

Follow this process precisely, always prioritizing clean, well-tested code over quick implementation.

Always write one test at a time, make it run, then improve structure. Always run all the tests (except long-running tests) each time.
