---
name: strategic-design
description: DDD 전략적 설계 패턴 가이드. Bounded Context 정의, Context Mapping, Subdomain 분류, 도메인 간 이벤트 통신, Anti-Corruption Layer 패턴. 새 Bounded Context 추가, 도메인 간 관계 설계, 이벤트 기반 통합, 외부 시스템 연동 시 사용. bounded context, context map, subdomain, ubiquitous language, event storming, ACL, anti-corruption, domain event, 도메인 경계, 컨텍스트 매핑 키워드에 트리거.
---

# Strategic Design — DDD 전략적 설계 패턴

복잡한 도메인을 관리 가능한 단위로 분해하고, 그 사이의 관계를 설계하는 가이드.
"어떻게 도메인을 나눌 것인가?"에 대한 답을 제공한다.

## 핵심 원칙

### Bounded Context (경계된 컨텍스트)

특정 도메인 모델이 적용되는 의미적 경계. 같은 단어가 다른 컨텍스트에서 다른 의미를 가질 수 있고, 이는 자연스러운 것이다.

```
이 프로젝트의 Bounded Context 구조:

domain/
├── auth/          # 인증 컨텍스트 (admin, oidc, signin)
├── club/          # 클럽 컨텍스트 (club, schedule, chat, feed, recruitment, sport, stats)
├── home/          # 홈 컨텍스트 (card, notice, schedule 집계)
├── notification/  # 알림 컨텍스트
├── users/         # 사용자 컨텍스트
└── shorts/        # 숏폼 컨텍스트
```

각 컨텍스트는:
- **자체 유비쿼터스 언어**를 가진다
- **자체 모델**을 가진다 (같은 개념이라도 컨텍스트마다 다르게 표현)
- **자체 데이터 소유권**을 가진다

### Ubiquitous Language (유비쿼터스 언어)

개발자와 도메인 전문가가 공유하는 언어. 코드, 문서, 대화에서 동일하게 사용한다.

```java
// 나쁨: 기술적 언어
club.setMemberStatus(userId, 2);

// 좋음: 유비쿼터스 언어
club.assignCoach(userId);
club.delegatePresident(currentPresidentId, targetUserId);
```

코드의 메서드명이 도메인 전문가와의 대화에서 사용하는 용어와 일치해야 한다. "코치를 지정한다", "회장을 위임한다"가 코드에 그대로 반영된다.

## Subdomain 분류

Subdomain은 **발견**하는 것이지 설계하는 것이 아니다.

| 유형 | 설명 | 투자 수준 | 전략 |
|------|------|----------|------|
| **Core** | 경쟁 우위, 차별화 요소 | 높음 | 직접 구축, 최고 개발자 투입 |
| **Supporting** | 필요하지만 차별화 아님 | 중간 | 직접 구축하되 간결하게 |
| **Generic** | 범용, 누구나 같은 방식 | 낮음 | SaaS/라이브러리 활용 |

### 판별 질문

```
이 도메인은 어떤 유형인가?
├─ "이걸로 경쟁사와 차별화되는가?"     → Core
├─ "필요하지만 우리만의 방식은 아닌가?" → Supporting
└─ "모든 서비스가 동일하게 필요한가?"   → Generic
```

### 이 프로젝트의 분류

| Subdomain | 분류 | 근거 |
|-----------|------|------|
| Club | **Core** | 동호회 관리의 핵심. 멤버 역할/권한, 복잡한 비즈니스 규칙 |
| Schedule | **Core** | 스포츠 클럽 관리의 본질. 출석 추적, 일정 생명주기 |
| Chat | **Core** | 커뮤니티 참여의 핵심. SSE 실시간 통신, Redis 캐싱 |
| Stats | **Core** | 스포츠 클럽 차별화 요소. 경기 통계가 참여를 유도 |
| Recruitment | Supporting | 멤버십 확장 보조. 가입 신청/승인 워크플로우 |
| Feed | Supporting | Schedule + 사용자 게시물 집계. Core 파생 콘텐츠 |
| Shorts | Supporting | UGC, 참여 강화. 핵심은 아님 |
| Sport/Football | Supporting | 스포츠별 확장. 라인업, 스쿼드 |
| Auth | Generic | 표준 인증/인가. Auth0 등으로 대체 가능 |
| Users | Generic | 기본 프로필 관리. 범용 패턴 |
| Notification | Generic | 표준 푸시 알림. Firebase/Twilio로 대체 가능 |
| Home | Generic | 읽기 전용 쿼리 파사드. 비즈니스 로직 없음 |

## Context Mapping

Bounded Context 간의 관계를 정의한다.

### 관계 패턴

| 패턴 | 설명 | 이 프로젝트 예시 |
|------|------|-----------------|
| **이벤트 기반 통합** | Domain Event로 비동기 소통 | Club → Chat (FoundClubEvent) |
| **Customer-Supplier** | 상류가 하류에 API 제공 | Club(상류) → Notification(하류) |
| **Conformist** | 하류가 상류 모델에 맞춤 | Auth → 외부 OIDC Provider |
| **ACL (Anti-Corruption Layer)** | 번역 계층으로 모델 보호 | HttpOidcProviders → OIDC Provider |
| **Shared Kernel** | 모델의 일부를 공유 | global/ddd/ (AggregateRoot, DomainEvent) |
| **Open Host Service** | 잘 정의된 API 공개 | REST API (/api/v1/*) |

### 이 프로젝트의 Context Map

```
                    External Systems
                    ┌──────────────────────┐
                    │ Google/Apple OIDC     │
                    │ Firebase (FCM)        │
                    └──────┬───────────────┘
                           │ ACL (HttpOidcProviders)
                           ▼
┌─────────┐  WithdrawnUserEvent  ┌──────────┐
│  Users  │ ───────────────────> │   Auth   │
└─────────┘                      └──────────┘

┌──────────────────────────────────────────────────────────┐
│                     Club Context                          │
│                                                          │
│  Club ──FoundClubEvent────────> Chat (ChatRoom 생성)     │
│       ──FoundClubEvent────────> Squad (스쿼드 생성)      │
│       ──FoundClubEvent────────> Lineup (라인업 생성)     │
│       ──AddedMemberEvent──────> Chat (멤버 추가)         │
│       ──AddedMemberEvent──────> Squad (선수 추가)        │
│       ──RemovedMemberEvent────> Chat (멤버 제거)         │
│       ──RemovedMemberEvent────> Squad (선수 제거)        │
│                                                          │
│  Schedule ──CreatedEvent──────> Feed (게시글 생성)       │
│           ──CreatedEvent──────> Notification (알림)      │
│           ──UpdatedEvent──────> Feed (게시글 수정)       │
│           ──CancelledEvent────> Feed (게시글 삭제)       │
│                                                          │
│  Recruitment ──ApprovedEvent──> Club (멤버 추가)         │
└──────────────────────────────────────────────────────────┘
                           │
                    이벤트 구독
                           ▼
                   ┌──────────────┐
                   │ Notification  │ (수동적 소비자)
                   └──────────────┘

┌──────────┐
│   Home   │ ← 여러 도메인 데이터를 조회 (Query Facade)
└──────────┘
```

## 이벤트 기반 통합 패턴

### 이벤트 발행-구독 흐름

```
1. Aggregate 내부에서 이벤트 수집
   club.addMember(member);  // → addEvent(new AddedClubMemberEvent(...))

2. Repository.save()에서 이벤트 발행
   club.publish(domainEventPublisher);  // → Spring ApplicationEventPublisher

3. @TransactionalEventListener로 수신
   @TransactionalEventListener
   public void on(AddedClubMemberEvent event) { ... }
```

### Retry 전략

이벤트 소비자는 `RetryTemplate`로 실패 시 재시도한다.

```java
// Chat 소비자 — 최대 10회, 지수 백오프
@TransactionalEventListener
public void on(final AddedClubMemberEvent event) {
    retryTemplate.execute(context -> {
        chatRoom.addChatter(event.member().getUserId());
        chatRoomRepository.save(chatRoom);
        return null;
    });
}
```

### 비동기 처리

독립적 트랜잭션이 필요한 경우 `@Async` + `REQUIRES_NEW`를 사용한다.

```java
@Async
@Transactional(propagation = Propagation.REQUIRES_NEW)
@TransactionalEventListener
public void on(final AttendanceStatusChangedEvent event) {
    // 출석 히스토리 기록 — 메인 트랜잭션과 무관하게 처리
}
```

## Anti-Corruption Layer (ACL)

외부 시스템의 모델이 도메인으로 침투하는 것을 방지하는 번역 계층.

### 이 프로젝트의 ACL 패턴

```
Domain Layer (Port):
  OidcProviders (interface)
    └─ identifier(idToken, providerType) → subjectId

Infrastructure Layer (Adapter):
  HttpOidcProviders (implements OidcProviders)
    └─ JWT 파싱 → 공개키 검증 → subject 추출
    └─ 외부 모델(JWK, JWT claims)을 도메인 모델(subjectId)로 변환
```

**ACL이 필요한 징후:**
- 외부 API의 데이터 구조가 도메인 모델과 맞지 않을 때
- 외부 시스템 변경이 도메인에 영향을 미칠 위험이 있을 때
- 레거시 시스템과 통합할 때

## 참조 문서

| 파일 | 목적 |
|------|------|
| [references/BOUNDED-CONTEXT.md](references/BOUNDED-CONTEXT.md) | Bounded Context 설계 가이드, 경계 판별법, 새 컨텍스트 추가 절차 |
| [references/CONTEXT-MAPPING.md](references/CONTEXT-MAPPING.md) | Context Map 작성법, 관계 패턴 상세, 이벤트 통합 패턴 |
| [references/EVENT-STORMING.md](references/EVENT-STORMING.md) | Event Storming 워크숍 가이드, 도메인 발견 프로세스 |

새 Bounded Context 추가 시 → BOUNDED-CONTEXT.md를 먼저 읽고 경계를 정의.
도메인 간 관계 설계 시 → CONTEXT-MAPPING.md 참조.
새 기능 기획/도메인 발견 시 → EVENT-STORMING.md 참조.
