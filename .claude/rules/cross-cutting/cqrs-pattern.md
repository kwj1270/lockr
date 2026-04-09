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
