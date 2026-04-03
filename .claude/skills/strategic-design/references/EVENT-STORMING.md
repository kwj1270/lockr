# Event Storming — 도메인 발견 프로세스

## Event Storming이란

도메인 이벤트, Aggregate, Bounded Context를 발견하기 위한 워크숍 기법. 코드가 아닌 **대화와 모델링**에서 시작한다.

> "DDD는 협업이다. 도메인 전문가와의 모델링 세션은 코드 패턴만큼 중요하다." — Eric Evans

---

## 워크숍 구성 요소

```
🟧 주황색: Domain Event (과거형 — "ClubFounded", "MemberAdded")
🟦 파란색: Command (명령형 — "FoundClub", "AddMember")
🟨 노란색: Aggregate (명사 — "Club", "Schedule")
🟪 분홍색: External System
🟣 보라색: Problem / Question
🟩 초록색: Read Model — 결정에 필요한 정보 (Query)
🟡 연노랑: Actor — 커맨드를 실행하는 구체적 페르소나
💜 연보라: Policy — 자동 규칙 ("~할 때마다, ~한다")
```

### Policy (정책)

이벤트와 커맨드 사이에 위치하는 자동 규칙. **"Whenever {Event}, then {Command}"** 형식.

정책은 코드에서 `@TransactionalEventListener`로 직접 매핑된다.

```
[Domain Event] → 💜 Policy → [Command] → [Aggregate] → [Domain Event]

예시:
  FoundClubEvent → "클럽이 생성될 때마다 채팅방을 만든다" → CreateChatRoom → ChatRoom → CreatedChatRoomEvent
  ApprovedApplicationEvent → "가입이 승인될 때마다 멤버를 추가한다" → AddMember → Club → AddedClubMemberEvent
  CreatedScheduleEvent → "일정이 생성될 때마다 피드를 만든다" → CreateFeed → Feed
```

**이 프로젝트의 주요 Policy 목록:**

| Policy | 트리거 Event | 결과 |
|--------|-------------|------|
| 채팅방 자동 생성 | FoundClubEvent | ChatRoom.init() |
| 채팅방 멤버 동기화 | AddedClubMemberEvent | ChatRoom.addChatter() |
| 스쿼드 자동 생성 | FoundClubEvent (FOOT_BALL) | Squad.init() |
| 피드 자동 생성 | CreatedScheduleEvent | Feed 생성 |
| 피드 자동 삭제 | CancelledScheduleEvent | Feed 삭제 |
| 멤버 자동 추가 | ApprovedApplicationEvent | Club.addMember() |
| 인증 정보 정리 | WithdrawnUserEvent | Oidc/SignInToken 삭제 |

Policy를 식별하면 **Event Consumer 클래스가 자연스럽게 도출**된다.

---

## 워크숍 흐름

### Phase 1: 혼돈적 탐색 (Chaotic Exploration)

모든 참여자가 알고 있는 도메인 이벤트를 자유롭게 나열한다.

**이 프로젝트 예시:**

```
ClubFounded, MemberAdded, MemberRemoved, CoachAssigned,
PresidentDelegated, ScheduleCreated, ScheduleUpdated,
ScheduleCancelled, AttendanceResponded, ChatMessageSent,
ChatRoomCreated, FeedPosted, RecruitmentOpened,
ApplicationSubmitted, ApplicationApproved, ApplicationRejected,
MatchRecorded, StatsUpdated, ShortsUploaded, ShortsReported,
UserSignedIn, UserSignedUp, UserWithdrawn, NotificationSent
```

**팁:** 이 단계에서는 정리하지 않는다. 중복, 모호함을 허용한다.

### Phase 2: 타임라인 정렬 (Timeline Ordering)

이벤트를 시간 순서대로 배치한다.

```
UserSignedUp → UserSignedIn → ClubFounded → MemberAdded
→ ScheduleCreated → AttendanceResponded → MatchRecorded
→ StatsUpdated → FeedPosted → ...
```

### Phase 3: Aggregate 식별

관련된 이벤트를 그룹으로 묶고, 어떤 Aggregate가 이벤트를 발행하는지 식별한다.

```
[Club Aggregate]
├── ClubFounded
├── MemberAdded
├── MemberRemoved
├── CoachAssigned
├── PresidentDelegated
└── VisibilityChanged

[Schedule Aggregate]
├── ScheduleCreated
├── ScheduleUpdated
├── ScheduleCancelled
└── AttendanceResponded

[Application Aggregate]
├── ApplicationSubmitted
├── ApplicationApproved
└── ApplicationRejected
```

### Phase 4: 경계 발견 (Boundary Finding)

**언어가 바뀌는 곳 = Bounded Context 경계**

```
"멤버" →
  Club에서: 역할(회장, 코치)을 가진 구성원
  Chat에서: 채팅방 참여자 (Chatter)
  Schedule에서: 출석 응답자 (Attendance)

→ Club, Chat, Schedule은 서로 다른 모델로 "멤버"를 표현
→ 같은 Club Context 안이지만, subdomain별 모델이 다름
```

### Phase 5: 문제 표면화 (Problem Surfacing)

불명확한 영역, 의문점, 갈등을 표시한다.

```
🟣 "회비 납부 시 PG 연동이 필요한가, 수동 확인인가?"
🟣 "반복 일정을 수정하면 이미 생성된 일정에도 소급 적용하나?"
🟣 "클럽 삭제 시 관련 데이터는 어떻게 처리하나?"
```

---

## Event Storming → 코드 매핑

Event Storming의 결과물이 코드 구조로 어떻게 변환되는지:

| Event Storming | 코드 |
|---------------|------|
| 🟧 Domain Event | `domain/event/{PastTense}Event.java` (Record implements DomainEvent) |
| 🟦 Command | `application/command/{Action}Command.java` (Record) |
| 🟨 Aggregate | `domain/{Subdomain}.java` (extends AggregateRoot) |
| 🟪 External System | `infrastructure/{External}Adapter.java` (ACL) |
| 🟩 Read Model | `api/{Subdomain}QueryApi.java` (jOOQ 직접 조회) |
| Policy (자동 규칙) | `@TransactionalEventListener` Consumer |

### 예시: Club 도메인

```
Event Storming 결과:
  🟦 FoundClub → 🟨 Club → 🟧 ClubFounded → 🟪 Policy: ChatRoom 생성

코드 매핑:
  FoundClubCommand.java         ← 🟦 Command
  Club.java (AggregateRoot)     ← 🟨 Aggregate
  FoundClubEvent.java           ← 🟧 Domain Event
  ChatConsumer.on(FoundClubEvent) ← 🟪 Policy
```

---

## 새 기능 설계에 Event Storming 적용

### 1단계: 이벤트 나열

새 기능에서 발생할 수 있는 모든 이벤트를 과거형으로 나열한다.

```
예: "회비 관리" 기능
  DuesBillingCreated, DuesPaid, DuesPaymentCancelled,
  DuesBillingClosed, DuesOverdueNotified
```

### 2단계: Command 식별

각 이벤트를 발생시키는 명령을 식별한다.

```
CreateDuesBilling → DuesBillingCreated
PayDues → DuesPaid
CancelDuesPayment → DuesPaymentCancelled
CloseDuesBilling → DuesBillingClosed
```

### 3단계: Aggregate 결정

어떤 Aggregate가 이 이벤트들을 소유하는지 결정한다.

```
[DuesBilling Aggregate]
├── DuesBillingCreated
├── DuesPaid
├── DuesPaymentCancelled
└── DuesBillingClosed
```

### 4단계: Policy 식별

이벤트에 반응하는 자동 규칙을 식별한다.

```
DuesBillingCreated → Policy: 전체 멤버에게 알림 발송
DuesOverdue(시간 기반) → Policy: 미납 알림 발송
```

### 5단계: Bounded Context 결정

이 기능이 기존 Context에 속하는지, 새 Context가 필요한지 판단한다.

```
"회비는 클럽 없이 존재할 수 있는가?" → 아니오
→ Club Context의 subdomain으로 추가: domain/club/dues/
```

### 6단계: Context Map 업데이트

새 subdomain과 기존 도메인 간의 이벤트 흐름을 정의한다.

```
Club.Dues ──DuesBillingCreated──> Notification (알림 발송)
Club.Club ──AddedMemberEvent──> Club.Dues (새 멤버 결제 항목 추가?)
```

---

## Event Storming 문서화 템플릿

워크숍 결과를 `docs/event-storming/{feature-name}.md`에 기록한다.

```markdown
# Event Storming: {기능명}

## 일시
- YYYY-MM-DD

## 참여자
- {역할}: {이름}

## Domain Events
| 이벤트 | 트리거 | Aggregate |
|--------|--------|-----------|
| ... | ... | ... |

## Commands
| Command | 실행 조건 | 결과 Event |
|---------|----------|-----------|
| ... | ... | ... |

## Policies (자동 규칙)
| 트리거 Event | Policy | 결과 |
|-------------|--------|------|
| ... | ... | ... |

## 미해결 질문
- [ ] ...

## Bounded Context 결정
- 기존 Context에 추가 / 새 Context 생성
- 근거: ...

## Context Map 변경
- 새로운 이벤트 흐름: ...
```

---

## Event Storming을 건너뛰어도 되는 경우

| 상황 | 판단 |
|------|------|
| 단순 CRUD | Event Storming 불필요 — 바로 구현 |
| 기존 패턴의 반복 | 기존 코드 참조로 충분 |
| 1인 개발, 단순 기능 | 머릿속 모델링으로 충분 |
| 복잡한 비즈니스 규칙 | **반드시 수행** |
| 여러 도메인에 걸치는 기능 | **반드시 수행** |
| 팀 간 합의가 필요한 기능 | **반드시 수행** |
