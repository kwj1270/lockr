# Domain Event Consumer 배치 가이드

## TL;DR

- **in-process 이벤트**(Spring `ApplicationEvent`처럼): `application/`이 기본
- **외부 브로커**(Kafka/RabbitMQ): `infrastructure/adapter/in/messaging/` + `application/` 2단 구조
- **외부 I/O 릴레이**(FCM, SSE, Email): `infrastructure/`
- **Read Model / Projection**: `infrastructure/projection/`
- **Saga / Process Manager**: `application/saga/`

## 일반적 관습 — 학파별 입장

### 1) DDD 정석 (Vaughn Vernon, IDDD)

> "Domain Event는 Application Service가 구독한다."

- 컨슈머도 결국 "다른 Aggregate의 UseCase를 호출하는 오케스트레이션"
- 위치: `application/handler/` 또는 `application/listener/`

### 2) Hexagonal / Ports & Adapters

> "이벤트 수신은 Inbound Adapter다."

- HTTP Controller가 HTTP inbound adapter이듯, 이벤트 리스너는 messaging inbound adapter
- 위치: `infrastructure/adapter/in/messaging/`
- **핵심**: 어댑터는 얇게 (역직렬화 + UseCase 호출만), 로직은 Application에

### 3) Clean Architecture

> "Input Boundary = Interface Adapter."

- HTTP든 이벤트든 모두 외부에서 들어오는 신호
- 위치: `interfaces/` 또는 `presentation/` (Controller와 같은 층)

### 4) Axon / Event Sourcing 계열

> "Event Handler는 1급 컴포넌트."

- Read Model Projection, Saga, Process Manager가 각자 독립 책임
- 위치: `projection/`, `saga/`, `eventhandler/` 별도 디렉토리

## 컨슈머 역할별 배치 매트릭스

| 컨슈머 역할 | 권장 위치 | 근거 |
|-----------|----------|------|
| 다른 Aggregate UseCase 호출 (오케스트레이션) | `application/` | Application Service와 책임 동일 |
| 외부 브로커 → 내부 UseCase 브릿지 | `infrastructure/adapter/in/messaging/` | 메시징은 외부 시스템 |
| 외부 시스템 릴레이 (FCM, Email, SMS) | `infrastructure/` | 외부 I/O 어댑터 |
| Read Model / Projection | `infrastructure/projection/` 또는 `query/projection/` | 조회 최적화 모델 |
| Saga / Process Manager | `application/saga/` | 상태 있는 장기 워크플로 |
| BC 간 통합 + 모델 변환 | `application/acl/` | Anti-Corruption Layer |

## In-process vs. External Broker

### In-process (Spring `ApplicationEvent`)

- 같은 프로세스 내 pub/sub
- 레이어 구분이 상대적으로 느슨
- **기본은 `application/`**
- `infrastructure/`는 외부 I/O가 있는 경우만

### External Broker (Kafka / RabbitMQ / SNS)

**반드시 2단 구조로 분리**:

1. `infrastructure/adapter/in/messaging/XxxKafkaListener` — 역직렬화 + UseCase 호출만
2. `application/XxxEventHandler` — 실제 로직

어댑터를 얇게 유지하는 이유: 브로커를 바꿔도 코어가 바뀌지 않아야 한다.

## lockr-server 현재 배치

### 현재 상태

**api/ 레이어에 둔 컨슈머** (타 서브도메인 UseCase 재호출형):
- `club/club/api/ClubEventConsumer` — `ApprovedApplicationEvent`
- `club/feed/api/FeedEventConsumer` — Schedule 이벤트
- `club/chat/api/ChatConsumer` — Club 멤버 이벤트
- `club/sport/football/squad/api/SquadEventConsumer`
- `club/sport/football/lineup/api/LineupConsumer`
- `auth/signin/api/WithdrawnUserEventConsumer`
- `auth/signin/api/SignInTokenConsumer`

**infrastructure/ 레이어에 둔 컨슈머** (외부 I/O):
- `club/schedule/infrastructure/ScheduleNotificationEventConsumer` (FCM)
- `club/fee/infrastructure/FeeNotificationEventConsumer` (FCM)
- `club/schedule/infrastructure/ScheduleAttendanceEventConsumer`
- `club/chat/infrastructure/sse/ChatEventListener` (SSE)

### 정석 관점의 권장

| 컨슈머 | 현재 | 권장 | 이유 |
|--------|-----|------|------|
| `FeedEventConsumer` | `api/` | `application/` | 다른 Aggregate UseCase 재호출 = 오케스트레이션 |
| `ChatConsumer` | `api/` | `application/` | 동상 |
| `SquadEventConsumer` | `api/` | `application/` | 동상 |
| `ClubEventConsumer` | `api/` | `application/` | 동상 |
| `WithdrawnUserEventConsumer` | `api/` | `application/` (혹은 제거 — [02 문서](02-transaction-and-events.md) 참고) | same-tx라면 오케스트레이션으로 전환 검토 |
| `ScheduleNotificationEventConsumer` | `infrastructure/` | 유지 | FCM 외부 I/O |
| `FeeNotificationEventConsumer` | `infrastructure/` | 유지 | FCM 외부 I/O |
| `ChatEventListener(sse)` | `infrastructure/` | 유지 | SSE 외부 브로드캐스트 |
| `ScheduleAttendanceEventConsumer` | `infrastructure/` | `application/` | 외부 I/O 아님, 내부 집계 |

## 파일명·패키지 컨벤션

- 단일 파일: `{Subdomain}EventConsumer.java`
- 특정 이벤트 하나만 처리: `{EventName}Consumer.java` (예: `WithdrawnUserEventConsumer`)
- 외부 시스템 릴레이: `{Subdomain}{External}EventConsumer.java` (예: `FeeNotificationEventConsumer`)

## 결정 포인트

새 Consumer 추가 시 순서대로 확인:

1. **이 consumer가 외부 시스템(FCM/SSE/HTTP)과 직접 통신하는가?** → Yes: `infrastructure/`
2. **외부 브로커(Kafka 등)에서 메시지를 받는가?** → Yes: 2단 구조 (`infrastructure/adapter/in/messaging/` + `application/`)
3. **그 외?** → `application/`

그리고 `api/`는 가급적 피한다 — HTTP 진입점 전용으로 유지.
