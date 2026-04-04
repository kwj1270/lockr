# DDD Tactical Patterns — 상세 가이드

## Aggregate Root

### 설계 원칙

Aggregate Root는 일관성 경계(consistency boundary)이다. 하나의 트랜잭션에서 하나의 Aggregate만 변경한다.

```
Aggregate Root (Club)
├── Entity (Member)        ← 같은 트랜잭션
├── Value Object (MemberRole)
└── Domain Event (FoundClubEvent)

Aggregate Root (Schedule)  ← 별도 트랜잭션, Event로 연계
```

### 구현 체크리스트

- [ ] `extends AggregateRoot`
- [ ] `static init(...)` 팩토리 메서드 → ULID 생성 + 생성 이벤트
- [ ] 비즈니스 규칙은 메서드 안에서 검증 (`if (...) throw`)
- [ ] 상태 변경 시 `addEvent(new XxxEvent(...))`
- [ ] `equals()`/`hashCode()`는 ID 기반
- [ ] 외부 의존 없음 (순수 Java)

### 팩토리 메서드 패턴

생성자 대신 `init()` 팩토리 메서드를 사용하는 이유:
1. ULID 자동 생성을 캡슐화
2. 생성 이벤트 발행을 강제
3. 유효성 검증을 한 곳에서 처리

```java
public static Club init(final String foundUserId, final String name, ...) {
    final Club club = new Club(generateUlid(), foundUserId, name, ...);
    club.addEvent(new FoundClubEvent(club.id, club.foundUserId, ...));
    return club;
}
```

생성자는 DB에서 복원할 때 사용한다 (Repository의 `domain()` 메서드).

### Rich Domain Model

비즈니스 로직은 Service가 아닌 Entity 안에 있어야 한다.

```java
// 좋음: Entity에 행위가 있음
public void removeMember(final String userId) {
    if (isStaff(userId)) {
        throw new IllegalStateException("운영진은 탈퇴할 수 없습니다.");
    }
    members.removeIf(member -> member.isSame(userId));
    this.addEvent(new RemovedClubMemberEvent(this.id, userId));
}

// 나쁨: Service에서 로직 처리 (빈약한 도메인 모델)
// clubService.removeMember() {
//     if (club.getMembers().stream().anyMatch(m -> m.isStaff())) { throw ... }
//     club.getMembers().removeIf(m -> m.isSame(userId));
// }
```

---

## Entity

### 식별자 기반 동등성

Entity는 ID로 동일성을 판단한다.

```java
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
```

### ULID 생성

모든 Entity의 ID는 ULID를 사용한다.

```java
import static com.official.lockr.global.util.UlidUtils.generateUlid;

// 팩토리 메서드에서 생성
final Club club = new Club(generateUlid(), ...);
```

ULID의 장점: 시간순 정렬 가능, UUID보다 가독성 좋음, DB 인덱스 성능 우수.

---

## Aggregate 내부 Entity

Aggregate Root 안에서만 접근 가능한 Entity. 고유 ID를 가지고 상태가 변경될 수 있지만, 외부에서 직접 참조하지 않는다.

### 구현 패턴

```java
public class Member {
    private final String id;
    private final String userId;
    private MemberRole role;      // 역할은 변경 가능 (Aggregate 내부에서만)
    private final String clubId;
    private final String name;
    private String profileImage;

    // 팩토리 메서드로 역할별 생성
    public static Member president(String userId, String clubId, String name, String profileImage) {
        return new Member(generateUlid(), userId, MemberRole.PRESIDENT, clubId, name, profileImage, ...);
    }

    public static Member basic(String userId, String clubId, String name, String profileImage) {
        return new Member(generateUlid(), userId, MemberRole.BASIC, clubId, name, profileImage, ...);
    }

    // 행위
    public boolean isSame(String userId) { return this.userId.equals(userId); }
    public boolean isPresident() { return role == MemberRole.PRESIDENT; }
    public boolean isPresidency() { return role == MemberRole.PRESIDENT || role == MemberRole.VICE_PRESIDENT; }
    public boolean isStaff() { return isPresidency() || role == MemberRole.MANAGER || role == MemberRole.COACH; }

    // 상태 변경 (Aggregate Root를 통해서만 호출)
    public void assignCoach() { this.role = MemberRole.COACH; }
    public void assignManager() { this.role = MemberRole.MANAGER; }
    public void assignPresident() { this.role = MemberRole.PRESIDENT; }
}
```

### Enum as Value Object

```java
public enum MemberRole {
    PRESIDENT, VICE_PRESIDENT, MANAGER, COACH, BASIC;

    public boolean isPresident() { return this == PRESIDENT; }
    public boolean isBasic() { return this == BASIC; }
}
```

### 복합 Value Object (vo/ 디렉토리)

도메인에 VO가 여러 개일 때 `vo/` 디렉토리로 분리한다.

```
domain/
└── schedule/
    └── domain/
        └── vo/
            ├── MatchDetailData.java
            ├── TrainingDetailData.java
            ├── SocialDetailData.java
            └── ScheduleDetailData.java
```

---

## Domain Event

### 설계 원칙

Domain Event는 "이미 일어난 사실"을 기록한다. 과거형으로 이름 짓는다.

| 행위 | Event 이름 |
|------|-----------|
| 클럽 창단 | `FoundClubEvent` |
| 멤버 추가 | `AddedClubMemberEvent` |
| 멤버 탈퇴 | `RemovedClubMemberEvent` |
| 로그인 처리 | `ProcessedSignInEvent` |
| 회원 탈퇴 | `WithdrawnUserEvent` |

### 구현 패턴

```java
public interface DomainEvent {}

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

- Java Record로 구현 (불변, 간결)
- `DomainEvent` 마커 인터페이스 구현
- 이벤트에 필요한 데이터만 포함 (전체 Entity가 아님)

### 이벤트 발행 흐름

```
1. Entity 내 행위 실행 시 → addEvent(new XxxEvent(...))
2. Repository.save() 호출 시 → club.publish(domainEventPublisher)
3. SpringDomainEventPublisher → ApplicationEventPublisher.publishEvent()
4. @EventListener 메서드가 수신
```

### 이벤트 수신

```java
@Component
public class ClubEventConsumer {
    @EventListener
    public void on(final FoundClubEvent event) {
        // 초기 스쿼드 생성 등 부수효과
    }
}

@Component
public class WithdrawnUserEventConsumer {
    @EventListener
    public void on(final WithdrawnUserEvent event) {
        // 관련 데이터 정리
    }
}
```

**위치:** Event Consumer는 수신하는 쪽의 `api/` 또는 `infrastructure/`에 둔다.

---

## Repository

### 인터페이스 (Domain Layer)

```java
public interface ClubRepository {
    @Nullable
    Club findByName(final String name);
    Club save(final Club club);
    @Nullable
    Club findById(String id);
}
```

### 구현 (Infrastructure Layer)

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
}
```

### Entity ↔ Domain 변환

jOOQ generated Entity와 Domain POJO 간 변환은 `domain()` static 메서드로 처리한다.

```java
// jOOQ Entity → Domain POJO
private static Club domain(final ClubsEntity entity, final List<Member> members) {
    return new Club(
        entity.getId(),
        entity.getFoundUserId(),
        entity.getName(),
        entity.getSportType(),
        // ...
        members,
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getDeletedAt()
    );
}
```

### Upsert 패턴

```java
clubsDao.ctx()
    .insertInto(CLUBS)
    .set(CLUBS.ID, club.getId())
    .set(CLUBS.NAME, club.getName())
    // ... 모든 필드
    .onDuplicateKeyUpdate()
    .set(CLUBS.NAME, club.getName())
    // ... 업데이트할 필드 (ID, CREATED_AT 제외)
    .execute();
```

### 자식 Entity 동기화 (syncMembers 패턴)

Aggregate Root가 자식 Entity 컬렉션을 가질 때:

```java
private void syncMembers(final Club club) {
    // 1. 기존 멤버 ID 조회
    final List<String> existingIds = memberDao.ctx()
        .select(MEMBERS.ID).from(MEMBERS)
        .where(MEMBERS.CLUB_ID.eq(club.getId()))
        .fetchInto(String.class);

    final List<String> currentIds = club.getMembers().stream()
        .map(Member::getId).toList();

    // 2. 삭제 (기존에 있었지만 현재 없는)
    final List<String> toDelete = existingIds.stream()
        .filter(id -> !currentIds.contains(id)).toList();
    if (!toDelete.isEmpty()) {
        memberDao.ctx().deleteFrom(MEMBERS)
            .where(MEMBERS.ID.in(toDelete)).execute();
    }

    // 3. 추가/수정 (UPSERT)
    if (!club.getMembers().isEmpty()) {
        upsertMembers(club.getMembers());
    }
}
```

---

## Aggregate 경계 결정 가이드

### 같은 Aggregate에 넣어야 할 때

- 부모-자식 관계이고 부모 없이 존재할 수 없음 (Club ↔ Member)
- 동일 트랜잭션에서 일관성이 필수 (주문 ↔ 주문항목)
- 외부에서 직접 접근하지 않음 (Member는 항상 Club을 통해)

### 별도 Aggregate로 분리해야 할 때

- 독립적인 생명주기 (Club ↔ Schedule)
- ID 참조만으로 충분 (Schedule에서 clubId만 가짐)
- 서로 다른 트랜잭션 (일정 생성과 클럽 수정이 동시에 일어나지 않음)
- 성능 이슈 (Aggregate가 너무 크면 분리)

### Cross-Aggregate 연계

Domain Event를 통해 최종 일관성(eventual consistency)을 달성한다.

```
Club Aggregate                  Schedule Aggregate
    │                               │
    ├── FoundClubEvent ──────────► @EventListener
    │                               └── 기본 스케줄 설정 생성
    │
    ├── AddedClubMemberEvent ───► @EventListener
    │                               └── 스쿼드 멤버 초기화
```
