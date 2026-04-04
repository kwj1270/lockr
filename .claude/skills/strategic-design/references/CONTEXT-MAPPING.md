# Context Mapping — 도메인 간 관계 설계

## Context Map이란

Bounded Context 간의 관계를 시각화한 다이어그램. 누가 누구에게 의존하는지, 어떤 방식으로 소통하는지를 보여준다.

---

## 관계 패턴 상세

### 1. 이벤트 기반 통합 (Domain Events)

**가장 많이 사용되는 패턴.** 컨텍스트 간 결합도를 최소화한다.

```
[발행자] ──DomainEvent──> [구독자]
  │                         │
  │ 이벤트만 알면 됨       │ 발행자를 몰라도 됨
  └─ 느슨한 결합           └─ 느슨한 결합
```

**이 프로젝트의 이벤트 흐름:**

| 발행 도메인 | 이벤트 | 구독 도메인 | 처리 |
|-------------|--------|-------------|------|
| Club | FoundClubEvent | Chat | ChatRoom 생성 |
| Club | FoundClubEvent | Sport/Squad | Squad 생성 |
| Club | FoundClubEvent | Sport/Lineup | Lineup 생성 |
| Club | AddedClubMemberEvent | Chat | Chatter 추가 |
| Club | AddedClubMemberEvent | Sport/Squad | SquadPlayer 추가 |
| Club | RemovedClubMemberEvent | Chat | Chatter 제거 |
| Club | RemovedClubMemberEvent | Sport/Squad | SquadPlayer 제거 |
| Schedule | CreatedScheduleEvent | Feed | SCHEDULE Feed 생성 |
| Schedule | UpdatedScheduleEvent | Feed | Feed 수정 |
| Schedule | CancelledScheduleEvent | Feed | Feed 삭제 |
| Schedule | CreatedScheduleEvent | Notification | 상대 클럽 알림 |
| Schedule | AttendanceStatusChangedEvent | Schedule/History | 출석 이력 기록 |
| Recruitment | ApprovedApplicationEvent | Club | 멤버 추가 |
| Auth | ProcessedSignInEvent | Auth/SignInToken | 토큰 생성 |
| Users | WithdrawnUserEvent | Auth | Oidc/SignInToken 삭제 |

### 2. Customer-Supplier (고객-공급자)

상류(upstream)가 하류(downstream)에 필요한 것을 제공한다. 하류가 요청하면 상류가 수용 여부를 결정한다.

```
Club (Upstream/Supplier)
  │
  ├─ "이런 이벤트를 발행할게"
  │
  ▼
Chat, Feed, Notification (Downstream/Customer)
  └─ "그 이벤트를 소비할게"
```

**이 프로젝트에서:** Club이 상류, Chat/Feed/Notification이 하류. Club 도메인의 이벤트 스키마가 변경되면 하류 소비자도 함께 수정해야 한다.

### 3. Conformist (순응자)

하류가 상류의 모델을 그대로 수용한다. 협상력이 없을 때.

```
External OIDC Provider (Google, Apple)
  │
  ├─ "JWT 형식은 이거야, JWK 스펙대로 해"
  │
  ▼
Auth Domain (Conformist)
  └─ "알겠어, 그 형식대로 파싱할게"
```

**이 프로젝트에서:** 외부 OIDC Provider의 JWT/JWK 스펙을 그대로 따른다. Google/Apple의 API가 변경되면 우리가 맞춰야 한다.

### 4. Anti-Corruption Layer (ACL)

외부 모델을 도메인 모델로 번역하는 계층.

```
External System ──> [ACL: Adapter + Translator] ──> Domain Model
```

**이 프로젝트 ACL 구현:**

```java
// Port (Domain Layer)
public interface OidcProviders {
    String identifier(String idToken, ProviderType providerType);
}

// Adapter (Infrastructure Layer) — ACL 역할
public class HttpOidcProviders implements OidcProviders {
    @Override
    public String identifier(String idToken, ProviderType providerType) {
        // 1. JWT 헤더에서 kid 추출
        // 2. Provider의 공개키 조회 (HttpOidcClient)
        // 3. RSA 공개키 복원
        // 4. JWT 검증 및 claims 파싱
        // 5. subject (도메인 모델)만 반환
        return claims.getSubject();  // 외부 모델 → 도메인 값
    }
}
```

**ACL 설계 원칙:**
- 도메인 레이어는 외부 시스템의 존재를 모른다
- Infrastructure에서 번역하여 도메인 값으로 변환
- 외부 API 변경 시 ACL만 수정하면 도메인은 무영향

### 5. Shared Kernel (공유 커널)

두 컨텍스트가 모델의 일부를 공유한다. 신중하게 사용해야 한다.

**이 프로젝트의 Shared Kernel: `global/` 패키지**

```
global/
├── ddd/
│   ├── AggregateRoot.java         # 모든 컨텍스트가 공유
│   ├── DomainEvent.java           # 이벤트 마커 인터페이스
│   └── DomainEventPublisher.java  # 발행 인터페이스
├── vo/
│   ├── Location.java              # 위치 VO
│   ├── Gender.java                # 성별 Enum
│   └── ...
└── util/
    ├── UlidUtils.java             # ID 생성
    └── SessionUtils.java          # 세션 유틸
```

**공유 커널 규칙:**
- 변경 시 모든 컨텍스트에 영향 → 매우 안정적이어야 함
- 비즈니스 로직 없음 — 기술적 인프라만 공유
- 도메인 특화 VO는 해당 컨텍스트 안에 둔다 (공유 금지)

### 6. Partnership (파트너십)

두 컨텍스트가 함께 성공하거나 함께 실패한다. 양 팀이 긴밀하게 조율한다.

```
[Context A] <──Partnership──> [Context B]
  │                              │
  └── 공동 계획, 공동 성공 ──────┘
```

**적용 시점:** 두 도메인이 동시에 변경되어야 하고, 한쪽만 배포할 수 없을 때.

### 7. Separate Ways (분리된 방법)

두 컨텍스트가 통합하지 않기로 결정한다. 각자 독립적으로 구현한다.

```
[Context A]     [Context B]
  │                 │
  └── 통합 없음 ────┘  (각자 구현)
```

**적용 시점:** 통합 비용이 통합 가치보다 클 때. 데이터 중복을 감수한다.

### 8. Open Host Service / Published Language

잘 정의된 프로토콜로 서비스를 공개한다.

**이 프로젝트:** REST API (`/api/v1/*`)가 Open Host Service 역할.

```
REST API (Published Language)
├── /api/v1/clubs/**        → Club Context
├── /api/v1/schedules/**    → Schedule Context
├── /api/v1/auth/**         → Auth Context
└── /api/v1/users/**        → Users Context
```

---

## Event Consumer 구현 패턴

### 기본 패턴: @TransactionalEventListener

```java
@Component
public class ChatConsumer {
    private final RetryTemplate retryTemplate;
    private final ChatRoomRepository chatRoomRepository;

    @TransactionalEventListener
    public void on(final FoundClubEvent event) {
        retryTemplate.execute(context -> {
            final ChatRoom chatRoom = ChatRoom.init(event.id(), event.name());
            chatRoomRepository.save(chatRoom);
            return null;
        });
    }
}
```

**`@TransactionalEventListener` vs `@EventListener`:**
- `@TransactionalEventListener`: 발행자의 트랜잭션 커밋 후 실행 (기본 AFTER_COMMIT)
- `@EventListener`: 발행자의 트랜잭션 내에서 동기 실행

대부분 `@TransactionalEventListener`을 사용한다. 발행자 트랜잭션이 롤백되면 이벤트도 소비되지 않는다.

### 비동기 패턴: @Async + REQUIRES_NEW

메인 트랜잭션과 독립적으로 처리해야 할 때:

```java
@Async
@Transactional(propagation = Propagation.REQUIRES_NEW)
@TransactionalEventListener
public void on(final AttendanceStatusChangedEvent event) {
    // 독립 트랜잭션에서 비동기 처리
    historyRepository.save(ScheduleAttendanceHistory.from(event));
}
```

### Retry 전략

이벤트 소비 실패 시 자동 재시도:

```java
// RetryTemplate 기본 설정 (global/config/RetryTemplateConfig)
// 글로벌 기본값 (RetryTemplateConfig):
// - 최대 재시도: 3회
// - 초기 간격: 1000ms
// - 지수 백오프: 1.5 (1000ms → 1500ms → 2250ms)
// - 최대 간격: 5000ms
//
// ChatConsumer 전용 (자체 RetryTemplate):
// - 최대 재시도: 10회
// - 초기 간격: 1000ms
// - 지수 백오프: 1.5
```

---

## 새 이벤트 통합 추가 절차

### 1. Domain Event 정의 (발행 측)

```java
// domain/{context}/{subdomain}/domain/event/
public record CreatedDuesBillingEvent(
    String billingId,
    String clubId,
    String title,
    int memberCount,
    LocalDateTime createdAt
) implements DomainEvent {}
```

### 2. Aggregate에서 이벤트 수집

```java
public static DuesBilling init(...) {
    DuesBilling billing = new DuesBilling(...);
    billing.addEvent(new CreatedDuesBillingEvent(...));
    return billing;
}
```

### 3. Event Consumer 작성 (구독 측)

```java
// 구독하는 도메인의 api/ 또는 infrastructure/ 에 위치
@Component
public class DuesNotificationConsumer {
    private final RetryTemplate retryTemplate;

    @TransactionalEventListener
    public void on(final CreatedDuesBillingEvent event) {
        retryTemplate.execute(context -> {
            // 알림 발송 등
            return null;
        });
    }
}
```

### 4. Context Map 문서 업데이트

`docs/domains/{context}-context.md`의 이벤트 섹션에 추가.

---

## Context Map 작성 가이드

### 다이어그램 형식

```
[Context A] ──{관계 유형}──> [Context B]
  │                            │
  발행: EventX, EventY        구독: EventX → 처리 Z
```

### 문서화 항목

| 항목 | 설명 |
|------|------|
| 발행 Context | 이벤트를 발행하는 쪽 |
| 구독 Context | 이벤트를 소비하는 쪽 |
| 이벤트 | Domain Event 클래스명 |
| 관계 유형 | Customer-Supplier, Conformist, ACL 등 |
| 동기/비동기 | @EventListener vs @TransactionalEventListener |
| 재시도 | RetryTemplate 설정 |
| 실패 처리 | 실패 시 대응 방안 (로그, 보상 트랜잭션 등) |
