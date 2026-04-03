# CQRS — Command/Query 분리 상세 패턴

## 개요

Command Query Responsibility Segregation. 상태를 변경하는 Command와 데이터를 조회하는 Query를 서로 다른 경로로 처리한다.

이 프로젝트에서는 **Simplified CQRS**를 사용한다: 같은 DB, 다른 쿼리 경로.

```
                    ┌─────────────────────────────────────┐
                    │              API Layer               │
                    ├──────────────────┬──────────────────┤
                    │   *Api.java      │  *QueryApi.java  │
                    │   (Command)      │  (Query)         │
                    ├──────────────────┼──────────────────┤
                    │                  │                  │
                    │   UseCase        │  (없음)          │
                    │      ↓           │                  │
                    │   Service        │                  │
                    │      ↓           │                  │
                    │   Repository     │  jOOQ DAO        │
                    │   (interface)    │  (ctx() 직접)    │
                    │      ↓           │                  │
                    │   JOOQ Impl     │                  │
                    ├──────────────────┴──────────────────┤
                    │           MySQL (같은 DB)            │
                    └─────────────────────────────────────┘
```

---

## Command vs Query 판별 기준

| 기준 | Command | Query |
|------|---------|-------|
| HTTP 메서드 | POST, PUT, DELETE | GET |
| DB 변경 | O | X |
| 이벤트 발행 | O (가능) | X |
| Service 레이어 | O (UseCase 경유) | X (DAO 직접) |
| 트랜잭션 | O (@Transactional) | X (읽기 전용) |
| Controller | `*Api.java` | `*QueryApi.java` |

**예외:** Query에서 이벤트 발행이 필요한 경우 → Command로 분류하거나 별도 처리.

---

## Command 상세 흐름

```
Request → Api → request.toCommand(userId) → useCase.method(command) → Service → Repository → JOOQ Impl
```

### 1단계: Request DTO → Command 변환

```java
public record FoundClubRequest(
    String name, String sportType, String city, ...
) {
    public FoundClubCommand toCommand(String userId) {
        return new FoundClubCommand(userId, name, sportType, city, ...);
    }
}
```

- Request DTO는 API 레이어에 속함 (`api/dto/`)
- `toCommand()` 메서드로 Application 레이어의 Command로 변환
- `userId`는 `SignInSession`에서 추출하여 전달

### 2단계: Controller → UseCase 호출

```java
@PostMapping
public ResponseEntity<Club> found(
    @RequestAttribute("signInSession") final SignInSession signInSession,
    @RequestBody final FoundClubRequest request
) {
    final Club club = foundClubUseCase.found(request.toCommand(signInSession.userId()));
    return ResponseEntity.created(URI.create("/api/v1/clubs/" + club.getId())).body(club);
}
```

- Controller는 UseCase 인터페이스에만 의존 (Service 구현체가 아님)
- 응답은 도메인 객체를 직접 반환하거나 Response DTO로 변환

### 3단계: Service 오케스트레이션

```java
@Override
public Club found(final FoundClubCommand command) {
    // 1. 중복 검증
    final Club existingClub = clubRepository.findByName(command.name());
    if (nonNull(existingClub)) return existingClub;

    // 2. 도메인 객체 생성 (비즈니스 로직은 도메인에 위임)
    final Club club = Club.init(command.userId(), command.name(), ...);

    // 3. 추가 행위
    club.addMember(president(command.userId(), club.getId(), null, profileImage));

    // 4. 저장 (이벤트 발행 포함)
    return clubRepository.save(club);
}
```

### 4단계: Repository 저장 + 이벤트 발행

```java
@Transactional
@Override
public Club save(final Club club) {
    upsertClub(club);                      // INSERT ... ON DUPLICATE KEY UPDATE
    syncMembers(club);                     // 자식 Entity 동기화
    club.publish(domainEventPublisher);    // Domain Event 발행 + 이벤트 리스트 clear
    return club;
}
```

이벤트 발행이 Repository의 `save()` 안에서 이루어지는 이유:
- "저장이 성공하면 이벤트도 발행된다"는 일관성 보장
- Service에서 `save()` 후 이벤트 발행을 잊는 실수 방지
- 트랜잭션 경계 안에서 처리

---

## Query 상세 흐름

```
Request → QueryApi → dao.ctx().select(...).fetchInto(DTO.class) → Response
```

### jOOQ DAO 활용 패턴

```java
@RestController
@RequestMapping("/api/v1/clubs")
public class ClubQueryApi {
    private final ClubsDao clubsDao;

    // Configuration DI → DAO 생성
    public ClubQueryApi(final Configuration configuration) {
        this.clubsDao = new ClubsDao(configuration);
    }
}
```

### 단순 조회: fetchInto

```java
@GetMapping
public ResponseEntity<FindClubsResponse> clubs(...) {
    return ResponseEntity.ok(new FindClubsResponse(
        clubsDao.ctx()
            .select(CLUBS)
            .from(CLUBS)
            .where(CLUBS.NAME.eq(name))
            .and(CLUBS.DELETED_AT.isNull())
            .fetchInto(FindClubResponse.class)
    ));
}
```

### JOIN + 매핑: .map()

```java
@GetMapping("/{clubId}/members")
public ResponseEntity<MembersResponse> getMembers(...) {
    final List<MemberResponse> members = clubsDao.ctx()
        .select(
            MEMBERS.USER_ID,
            DSL.coalesce(MEMBERS.NAME, USER_ADDITIONAL_INFO.NAME).as("name"),
            MEMBERS.MEMBER_ROLE,
            MEMBERS.PROFILE_IMAGE
        )
        .from(MEMBERS)
        .leftJoin(USER_ADDITIONAL_INFO)
            .on(MEMBERS.USER_ID.eq(USER_ADDITIONAL_INFO.USER_ID))
        .where(MEMBERS.CLUB_ID.eq(clubId))
        .and(MEMBERS.DELETED_AT.isNull())
        .fetch()
        .map(record -> new MemberResponse(
            record.get(MEMBERS.USER_ID),
            record.get("name", String.class),
            record.get(MEMBERS.MEMBER_ROLE),
            record.get(MEMBERS.PROFILE_IMAGE)
        ));

    return ResponseEntity.ok(new MembersResponse(members));
}
```

### Cursor 기반 페이지네이션

```java
var query = clubsDao.ctx()
    .select(CLUBS.ID, MEMBERS.MEMBER_ROLE)
    .from(CLUBS)
    .innerJoin(MEMBERS).on(MEMBERS.CLUB_ID.eq(CLUBS.ID))
    .where(MEMBERS.USER_ID.eq(userId))
    .and(CLUBS.DELETED_AT.isNull());

if (!cursor.isEmpty()) {
    query = query.and(CLUBS.ID.lt(cursor));
}

final var result = query
    .orderBy(CLUBS.CREATED_AT.desc())
    .limit(limit)
    .fetch();
```

---

## jOOQ 코드 사용 규칙

### Generated Table 상수

```java
import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
```

- 테이블 상수는 `{TableName}JOOQEntity.{TABLE_NAME}` 형식
- static import로 사용

### Generated Entity (POJO)

```java
import org.jooq.generated.tables.pojos.ClubsEntity;
import org.jooq.generated.tables.pojos.MembersEntity;
```

- `fetchInto(ClubsEntity.class)` → jOOQ generated POJO로 매핑
- `J` prefix 네이밍 전략 (jOOQ codegen 설정)

### Generated DAO

```java
import org.jooq.generated.tables.daos.ClubsDao;
```

- `new ClubsDao(configuration)` → DAO 생성
- `dao.ctx()` → `DSLContext` 접근

---

## Domain Event 소비

다른 도메인의 이벤트를 수신하여 부수효과를 처리한다.

```java
@Component
public class ClubEventConsumer {

    @EventListener
    public void on(final FoundClubEvent event) {
        // 클럽 생성 시 초기 데이터 세팅 등
    }
}
```

- Spring `@EventListener` 사용
- Event Consumer는 `api/` 또는 `infrastructure/`에 위치
- 같은 트랜잭션 내에서 실행됨 (비동기 필요 시 `@Async` 추가)