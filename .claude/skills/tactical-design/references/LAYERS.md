# Layer Structure — 상세 구현 가이드

각 레이어별 역할, 규칙, 실제 코드 패턴을 정리한다.

---

## 1. Domain Layer (최내층)

시스템의 핵심. 비즈니스 로직과 규칙을 담으며, **외부 의존성이 전혀 없다** (순수 Java).

### 1.1 Aggregate Root

모든 Aggregate Root는 `AggregateRoot`를 상속하고, 팩토리 메서드(`init`)로 생성한다.

```java
public abstract class AggregateRoot {
    private final List<DomainEvent> events;

    public AggregateRoot() {
        this(new ArrayList<>());
    }

    public AggregateRoot(final List<DomainEvent> events) {
        this.events = events;
    }

    protected void addEvent(DomainEvent event) {
        events.add(event);
    }

    public void publish(DomainEventPublisher publisher) {
        events.forEach(publisher::publish);
        events.clear();
    }
}
```

**Aggregate Root 구현 패턴:**

```java
public class Club extends AggregateRoot {
    private final String id;
    private final String foundUserId;
    private String name;
    // ... 필드

    // 팩토리 메서드 — 생성 시 이벤트 발행
    public static Club init(final String foundUserId, final String name, ...) {
        final Club club = new Club(generateUlid(), foundUserId, name, ...);
        club.addEvent(new FoundClubEvent(club.id, club.foundUserId, ...));
        return club;
    }

    // 비즈니스 행위 — 도메인 규칙을 Entity 안에서 강제
    public void addMember(final Member member) {
        members.add(member);
        this.addEvent(new AddedClubMemberEvent(member, sportType));
    }

    public void removeMember(final String userId) {
        if (isStaff(userId)) {
            throw new IllegalStateException("운영진은 탈퇴할 수 없습니다.");
        }
        members.removeIf(member -> member.isSame(userId));
        this.addEvent(new RemovedClubMemberEvent(this.id, userId));
    }

    // Identity 기반 동등성
    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Club club = (Club) o;
        return Objects.equals(getId(), club.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
```

**핵심 규칙:**
- `init()` 팩토리 메서드로만 새 인스턴스 생성 → ULID 자동 생성 + 생성 이벤트 발행
- 비즈니스 규칙은 Entity 메서드 안에서 검증 (`if (!isPresidency) throw`)
- 상태 변경 시 `addEvent()`로 Domain Event 수집
- `equals()`/`hashCode()`는 ID 기반

### 1.2 Domain Event

`DomainEvent` 마커 인터페이스를 구현하는 Java Record.

```java
public interface DomainEvent {}

// 과거형 이름 사용
public record FoundClubEvent(
    String id,
    String foundUserId,
    String name,
    String sportType,
    String city,
    String district,
    String description,
    LocalDateTime createdAt
) implements DomainEvent {}
```

**명명 규칙:** `{과거분사}{도메인}Event` — `FoundClubEvent`, `AddedClubMemberEvent`, `ProcessedSignInEvent`

### 1.3 Repository Interface

도메인 레이어에서 인터페이스만 정의한다. 구현은 Infrastructure에서.

```java
public interface ClubRepository {
    @Nullable
    Club findByName(final String name);

    Club save(final Club club);

    @Nullable
    Club findById(String id);
}
```

**규칙:**
- Aggregate당 1개 Repository
- 반환/파라미터는 도메인 객체만 (`Club`, 절대 `ClubsEntity`가 아님)
- `@Nullable`로 null 반환 가능성 명시

### 1.4 Value Object

불변 객체. 값 기반 동등성을 가진다.

```java
public class Member {
    private final String id;
    private final String userId;
    private MemberRole role;
    private final String clubId;
    // ...

    // 팩토리 메서드
    public static Member president(String userId, String clubId, String name, String profileImage) {
        return new Member(generateUlid(), userId, MemberRole.PRESIDENT, clubId, name, profileImage, ...);
    }

    public static Member basic(String userId, String clubId, String name, String profileImage) {
        return new Member(generateUlid(), userId, MemberRole.BASIC, clubId, name, profileImage, ...);
    }

    // 행위
    public void assignCoach() { this.role = MemberRole.COACH; }
    public void assignManager() { this.role = MemberRole.MANAGER; }
    public boolean isPresident() { return role == MemberRole.PRESIDENT; }
    public boolean isSame(String userId) { return this.userId.equals(userId); }
}
```

---

## 2. Application Layer

도메인 객체를 조율하여 유스케이스를 구현한다. jOOQ 의존 없음.

### 2.1 UseCase Interface

하나의 인터페이스에 하나의 메서드. Driver Port 역할을 한다.

```java
public interface FoundClubUseCase {
    Club found(final FoundClubCommand command);
}

public interface RegisterClubMemberUseCase {
    Club addMember(final AddMemberCommand command);
}
```

### 2.2 Command (Record)

불변 DTO. 유스케이스에 필요한 데이터만 담는다.

```java
public record FoundClubCommand(
    String userId,
    String name,
    String sportType,
    String city,
    String district,
    String description,
    String profileImageUrl,
    String backgroundImageUrl
) {}
```

### 2.3 Service

여러 UseCase를 구현하는 클래스. Repository 인터페이스만 의존한다.

```java
@Service
public class ClubService implements FoundClubUseCase, RegisterClubMemberUseCase, ... {

    private final ClubRepository clubRepository;    // Domain interface
    private final UsersRepository usersRepository;  // 다른 Aggregate 참조 시

    public ClubService(final ClubRepository clubRepository, final UsersRepository usersRepository) {
        this.clubRepository = clubRepository;
        this.usersRepository = usersRepository;
    }

    @Override
    public Club found(final FoundClubCommand command) {
        final Club existingClub = clubRepository.findByName(command.name());
        if (nonNull(existingClub)) {
            return existingClub;
        }
        final Club club = Club.init(
            command.userId(), command.name(), command.sportType(),
            command.city(), command.district(), command.description(),
            command.profileImageUrl(), command.backgroundImageUrl()
        );
        club.addMember(president(command.userId(), club.getId(), null, founderProfileImage));
        return clubRepository.save(club);
    }
}
```

**핵심 규칙:**
- `@Service` 어노테이션
- 생성자 주입 (final 필드)
- Repository 인터페이스만 의존 (jOOQ DAO 아님)
- 비즈니스 행위는 도메인 객체에 위임 (`club.addMember(...)`)
- Service는 오케스트레이션만 담당

---

## 3. API Layer (Presentation)

### 3.1 Command Controller (*Api.java)

UseCase 인터페이스를 주입받아 상태 변경 요청을 처리한다.

```java
@RestController
@RequestMapping("/api/v1/clubs")
public class ClubApi {
    private final FoundClubUseCase foundClubUseCase;
    // ... 다른 UseCase 주입

    @PostMapping
    public ResponseEntity<Club> found(
        @RequestAttribute("signInSession") final SignInSession signInSession,
        @RequestBody final FoundClubRequest request
    ) {
        final Club club = foundClubUseCase.found(request.toCommand(signInSession.userId()));
        return ResponseEntity.created(URI.create("/api/v1/clubs/" + club.getId())).body(club);
    }
}
```

**패턴:**
- Request DTO의 `toCommand()` 메서드로 Command 변환
- `@RequestAttribute("signInSession")`으로 인증 정보 획득
- UseCase 호출 후 결과 반환

### 3.2 Query Controller (*QueryApi.java)

jOOQ Configuration을 주입받아 DAO로 직접 조회한다. Service 레이어 없음.

```java
@RestController
@RequestMapping("/api/v1/clubs")
public class ClubQueryApi {
    private final ClubsDao clubsDao;

    public ClubQueryApi(final Configuration configuration) {
        this.clubsDao = new ClubsDao(configuration);
    }

    @GetMapping("/my")
    public ResponseEntity<MyClubsResponse> getMyClubs(
        @RequestAttribute("signInSession") final SignInSession signInSession,
        @RequestParam(value = "cursor", required = false) String cursor,
        @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        // jOOQ DSLContext로 직접 쿼리
        var query = clubsDao.ctx()
            .select(CLUBS.ID, MEMBERS.MEMBER_ROLE)
            .from(CLUBS)
            .innerJoin(MEMBERS).on(MEMBERS.CLUB_ID.eq(CLUBS.ID))
            .where(MEMBERS.USER_ID.eq(signInSession.userId()))
            .and(CLUBS.DELETED_AT.isNull());

        if (!cursor.isEmpty()) {
            query = query.and(CLUBS.ID.lt(cursor));
        }

        final var result = query.orderBy(CLUBS.CREATED_AT.desc())
            .limit(limit)
            .fetch();

        // 직접 Response DTO로 매핑
        return ResponseEntity.ok(new MyClubsResponse(/* ... */));
    }
}
```

**패턴:**
- `Configuration` DI → `new XxxDao(configuration)` → `dao.ctx()`로 DSLContext 접근
- `fetchInto(ResponseDTO.class)` 또는 `.map()` 으로 DTO 변환
- jOOQ generated table 상수 사용: `CLUBS`, `MEMBERS` 등
- cursor 기반 페이지네이션

### 3.3 Request DTO

`toCommand()` 메서드로 Command 변환을 캡슐화한다.

```java
public record FoundClubRequest(
    String name,
    String sportType,
    String city,
    String district,
    String description,
    String profileImageUrl,
    String backgroundImageUrl
) {
    public FoundClubCommand toCommand(String userId) {
        return new FoundClubCommand(userId, name, sportType, city, district, description, profileImageUrl, backgroundImageUrl);
    }
}
```

### 3.4 Event Consumer

다른 도메인의 Domain Event를 수신하여 처리한다.

```java
@Component
public class ClubEventConsumer {
    @EventListener
    public void on(final ProcessedSignInEvent event) {
        // 다른 Aggregate의 이벤트에 반응
    }
}
```

---

## 4. Infrastructure Layer

Domain/Application 레이어의 인터페이스를 구현한다.

### 4.1 JOOQ Repository

```java
@Repository
public class JOOQClubRepository implements ClubRepository {
    private final ClubsDao clubsDao;
    private final MembersDao memberDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQClubRepository(final Configuration configuration,
                              final DomainEventPublisher domainEventPublisher) {
        this.clubsDao = new ClubsDao(configuration);
        this.memberDao = new MembersDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional
    @Override
    public Club save(final Club club) {
        upsertClub(club);        // jOOQ INSERT ... ON DUPLICATE KEY UPDATE
        syncMembers(club);       // 자식 Entity 동기화
        club.publish(domainEventPublisher);  // Domain Event 발행
        return club;
    }

    @Nullable
    @Override
    public Club findById(final String id) {
        final var record = clubsDao.ctx().select(...)
            .from(CLUBS)
            .where(CLUBS.ID.eq(id))
            .fetchOne();
        if (Objects.isNull(record)) return null;
        return domain(record.into(ClubsEntity.class), findAllMember(id));
    }

    // Entity → Domain 변환 (static 메서드)
    private static Club domain(final ClubsEntity entity, final List<Member> members) {
        return new Club(
            entity.getId(), entity.getFoundUserId(), entity.getName(), ...
        );
    }
}
```

**핵심 패턴:**
- `Configuration` DI → DAO 생성
- `@Transactional`은 Repository에서 관리
- `save()`에서 upsert + 자식 동기화 + 이벤트 발행을 한 트랜잭션으로 처리
- `domain()` static 메서드로 jOOQ Entity → Domain POJO 변환
- Upsert 패턴: `insertInto(...).set(...).onDuplicateKeyUpdate().set(...)`

### 4.2 Domain Event Publisher

Spring의 `ApplicationEventPublisher`를 래핑한다.

```java
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
```

### 4.3 Anti-Corruption Layer (ACL) 구현

외부 모델이 도메인으로 침투하는 것을 방지하는 변환 계층. 동기 ACL과 이벤트 ACL 두 가지 유형이 있다.

#### 동기 ACL — 외부 API 호출

내가 외부 시스템을 호출하고 결과를 내부 모델로 변환할 때. Port(domain) + Adapter(infrastructure) 패턴.

```java
// Domain Layer — Port (인터페이스)
public interface OidcProviders {
    String identifier(String idToken, ProviderType providerType);
}

// Infrastructure Layer — ACL Adapter (구현체)
// 외부 모델(JWK, JWT claims)에 대한 의존은 이 클래스에만 존재
@Component
public class HttpOidcProviders implements OidcProviders {
    private final HttpOidcClient httpOidcClient;

    @Override
    public String identifier(String idToken, ProviderType providerType) {
        // JWT 파싱 → 공개키 검증 → subject 추출
        // 외부 모델 → 도메인 값 변환이 여기서 완료
        return claims.getSubject();
    }
}
```

**핵심:** Domain 레이어는 외부 시스템의 존재를 모른다. Infrastructure에서 변환하여 도메인 값만 반환한다.

#### 이벤트 ACL — 외부 도메인 이벤트 수신

다른 Bounded Context의 Domain Event를 수신하여 내부 Command로 변환할 때. EventConsumer(api 레이어)가 ACL 역할을 겸한다.

```java
// 단순화된 예시 — 실제 클래스명과 다를 수 있음
// api/ 레이어에 위치
@Component
public class ScheduleNotificationConsumer {

    private final RegisterNotificationUseCase registerNotificationUseCase;

    // 외부 이벤트 타입(CreatedScheduleEvent)은 이 메서드 시그니처에만 존재
    @TransactionalEventListener
    public void on(final CreatedScheduleEvent event) {
        // ACL 변환: 외부 이벤트 → 내부 Command
        var command = new RegisterNotificationCommand(
            "새 일정: " + event.title(),
            event.clubName() + "에 새 일정이 등록되었습니다",
            NotificationType.SCHEDULE.name(),
            event.opponentClubId(),
            Map.of("scheduleId", event.scheduleId())
        );
        // UseCase 이하로 외부 타입이 전파되지 않음
        registerNotificationUseCase.register(command);
    }
}
```

**핵심:** EventConsumer 메서드 내에서 외부 이벤트 → 내부 Command 변환을 완료한다. UseCase 인터페이스의 파라미터에 외부 도메인 타입이 나타나면 ACL이 실패한 것이다.

**Consumer 위치 기준:**
- 단순 이벤트 변환만 수행 → `api/` 레이어 (ChatConsumer, FeedEventConsumer 패턴)
- 변환에 도메인 서비스(cross-context 조회 등) 필요 → `infrastructure/` 레이어 (ScheduleNotificationEventConsumer 패턴)

#### 이벤트 ACL이 복잡해질 때 — 전용 ACL 클래스 분리

이벤트 종류가 많거나 변환에 외부 조회(도메인 서비스)가 필요하면 ACL 클래스를 분리한다.

```java
// 단순화된 예시 — 실제 클래스명과 다를 수 있음
// api/ 레이어에 ACL 전용 클래스
@Component
public class NotificationACL {

    private final ClubQueryService clubQueryService;  // 도메인 서비스 인터페이스 (예시)

    // 메서드 오버로딩으로 이벤트 타입별 변환
    // 외부 이벤트 import는 이 클래스에만 집중
    public RegisterNotificationCommand translate(final CreatedScheduleEvent event) {
        String clubName = clubQueryService.findName(event.clubId());
        return new RegisterNotificationCommand(
            "새 일정: " + event.title(),
            clubName + "에 새 일정이 등록되었습니다",
            NotificationType.SCHEDULE.name(),
            event.opponentClubId(),
            Map.of("scheduleId", event.scheduleId())
        );
    }

    public RegisterNotificationCommand translate(final CancelledScheduleEvent event) {
        return new RegisterNotificationCommand(
            "일정 취소",
            event.title() + " 일정이 취소되었습니다",
            NotificationType.SCHEDULE.name(),
            event.opponentClubId(),
            Map.of("scheduleId", event.scheduleId())
        );
    }
}

// EventConsumer는 위임만
@Component
public class ScheduleNotificationConsumer {
    private final NotificationACL acl;
    private final RegisterNotificationUseCase useCase;

    @TransactionalEventListener
    public void on(final CreatedScheduleEvent event) {
        useCase.register(acl.translate(event));
    }

    @TransactionalEventListener
    public void on(final CancelledScheduleEvent event) {
        useCase.register(acl.translate(event));
    }
}
```

#### ACL 분리 기준

```
ACL 클래스를 분리할까?
├─ 이벤트 종류 ≤ 3개, 변환이 단순        → EventConsumer에서 직접 변환
├─ 이벤트 종류 ≥ 4개                      → ACL 클래스 분리
├─ 변환에 도메인 서비스 조회가 필요        → ACL 클래스 분리
└─ Strategy 패턴이 필요한가?              → 대부분 불필요, 메서드 오버로딩으로 충분
```

**Strategy 패턴보다 메서드 오버로딩을 권장하는 이유:**
- Strategy 패턴은 `isSupport()` + `convert()`로 외부 타입 체크가 전략 클래스마다 퍼짐 → 외부 의존이 분산됨
- 메서드 오버로딩은 ACL 클래스 1곳에서 외부 타입별 변환을 집중 관리 → 단일 의존점 원칙 충족
- 이벤트 타입이 10개 이상으로 늘어나는 극단적 경우에만 Strategy를 고려

---

## 새 도메인 추가 순서

1. **Domain 먼저** — Entity, Value Object, Repository Interface, Domain Event
2. **Application** — Command Record, UseCase Interface, Service 구현
3. **API** — Request/Response DTO, Command Controller, Query Controller
4. **Infrastructure** — JOOQ Repository 구현
5. **Flyway Migration** — `src/main/resources/db/migration/V{N}__{description}.sql`

이 순서는 의존성 방향을 따른다. 안쪽 레이어부터 구현하면 바깥 레이어는 자연스럽게 완성된다.