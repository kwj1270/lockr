---
globs:
  - "src/main/java/com/official/lockr/domain/**/*.java"
---

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

---

## Event 흐름 (Spring 이벤트 / Kafka Listener)

Command 흐름과 별개의 트랜잭션 컨벤션을 갖는다. **inbox 멱등 가드와 doHandle 비즈니스 로직의 원자성** 보장이 핵심.

```
[Event 흐름]
EventPublisher → IntegrationDomainEvent → Outbox → OutboxProcessor
                                                       ↓
                            ApplicationEventPublisher.publishEvent()
                                                       ↓
                IdempotentEventHandler<E> (In-adapter, @Transactional 소유)
                                                       ↓
                              doHandle() → useCase.handle() (Service POJO)
                                                       ↓
                                        Repository (REQUIRED join)
```

### 트랜잭션 위치 — In-adapter 소유 (Command 흐름과 다른 갈래)

| 흐름 | 트랜잭션 소유 | 근거 |
|------|------------|------|
| **Command** | Out-adapter (`JOOQ*Repository.save`) | 단일 Aggregate save = 단일 트랜잭션. Service POJO 유지 |
| **Event** | **In-adapter (`IdempotentEventHandler.onApplicationEvent`)** | inbox 가드 + doHandle 비즈니스 로직 원자성 보장 |

이 분기는 의도된 결정이다. Out-adapter만 트랜잭션 두면 inbox INSERT와 비즈니스 Repository.save가
별도 트랜잭션이 되어 doHandle 실패 시 inbox row만 commit → 이벤트 영구 유실.
근거 + 대안 비교는 **ADR-0008 (트랜잭션 위치 — In-adapter 선택 근거 섹션)** 참조.

### 헥사고날 어댑터 분류 정리

- **In-adapter (Driving)**: 외부 → 도메인. HTTP Controller, EventListener, Kafka Listener
- **Out-adapter (Driven)**: 도메인 → 외부. JOOQ Repository, HttpClient, FCM 발송기
- `infrastructure/` 패키지는 두 종류 모두 담음. 분류는 패키지가 아닌 **트래픽 방향**

### 구현 패턴

```java
// In-adapter (infrastructure/) — 트랜잭션 + 멱등 가드 (베이스가 흡수)
@Component
public class FeeNotificationEventConsumer extends IdempotentEventHandler<UnpaidFeeNotifiedEvent> {
    private final HandleUnpaidFeeNotifiedUseCase useCase;

    public FeeNotificationEventConsumer(IdempotentExecutor executor, HandleUnpaidFeeNotifiedUseCase useCase) {
        super(executor);
        this.useCase = useCase;
    }

    @Override protected String consumerName() { return "fee.unpaid_notification"; }

    @Override protected void doHandle(UnpaidFeeNotifiedEvent event) {
        useCase.handle(event);   // Service에 단순 위임
    }
}

// Service (POJO, @Transactional X) — 비즈니스 로직만
@Service
public class FeeNotificationEventService implements HandleUnpaidFeeNotifiedUseCase {
    @Override public void handle(UnpaidFeeNotifiedEvent event) {
        // 비즈니스 로직만 — 트랜잭션은 IdempotentEventHandler가 시작한 것에 join
    }
}
```

자식 Consumer는 `consumerName()` + `doHandle()` + 생성자만 구현하면 된다. `@EventListener`, `@Transactional` boilerplate는 베이스가 흡수.

## REFERENCE CODE

새 기능 구현 시 아래 파일들을 참고하여 일관된 스타일 유지:

### Domain Layer
- Entity: `domain/club/club/domain/Club.java`
- Value Object: `domain/club/club/domain/Member.java`
- Domain Event: `domain/club/club/domain/event/FoundClubEvent.java`
- Repository Interface: `domain/club/club/domain/ClubRepository.java`

### Application Layer
- Service: `domain/club/club/application/ClubService.java`
- UseCase: `domain/club/club/application/usecase/FoundClubUseCase.java`
- Command: `domain/club/club/application/command/FoundClubCommand.java`

### API Layer
- Controller: `domain/club/club/api/ClubApi.java`
- Query Controller: `domain/club/club/api/ClubQueryApi.java`
- Request DTO: `domain/club/club/api/dto/FoundClubRequest.java`
- Response DTO: `domain/club/club/api/dto/MyClubResponse.java`

### Infrastructure Layer
- Repository 구현: `domain/club/club/infrastructure/JOOQClubRepository.java`

### Test
- Domain Test: `test/.../club/schedule/domain/ScheduleTest.java`
