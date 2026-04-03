# Bounded Context — 설계 가이드

## Bounded Context란

특정 도메인 모델이 적용되는 의미적 경계. 경계 안에서 용어는 정확하고 모호하지 않은 의미를 가진다.

같은 단어가 다른 컨텍스트에서 다른 의미를 가지는 것은 자연스럽다:
- **Club 컨텍스트**: "멤버" = 역할(회장, 코치, 일반)을 가진 클럽 구성원
- **Auth 컨텍스트**: "멤버" = 인증된 사용자 세션
- **Chat 컨텍스트**: "멤버" = 채팅방 참여자 (Chatter)

---

## 경계 판별법

### "같은 컨텍스트인가, 분리해야 하나?"

```
같은 Bounded Context에 넣을 때:
├─ 같은 유비쿼터스 언어를 사용한다
├─ 같은 도메인 전문가가 담당한다
├─ 한 팀이 소유한다
└─ 변경 이유가 같다 (같이 바뀐다)

별도 Bounded Context로 분리할 때:
├─ 같은 용어가 다른 의미를 가진다
├─ 서로 다른 비즈니스 규칙이 적용된다
├─ 독립적인 배포/변경 주기를 가진다
└─ 다른 팀/도메인 전문가가 담당한다
```

### 이 프로젝트의 경계 판단 사례

**Club 내부 subdomain들이 하나의 Context인 이유:**
- 모두 "클럽"이라는 동일한 핵심 개념을 중심으로 동작
- Club 멤버, 일정, 채팅, 피드가 모두 같은 clubId를 공유
- 한 팀이 소유하고 같은 배포 주기

**Auth와 Users가 분리된 이유:**
- Auth: "누구인지 확인" (인증) — JWT 토큰, OIDC, 세션
- Users: "누구인지 설명" (프로필) — 이름, 생년월일, 추가 정보
- 변경 이유가 다름: Auth는 보안 정책 변경 시, Users는 프로필 기능 추가 시

**Notification이 독립 Context인 이유:**
- 모든 도메인의 이벤트를 소비하는 수동적(passive) 컨텍스트
- 알림 채널(FCM, SMS, 이메일) 변경이 다른 도메인에 영향 없음
- 독립적으로 진화 가능 (새 알림 유형 추가가 다른 도메인 변경 불필요)

---

## 새 Bounded Context 추가 절차

### 1단계: 경계 정의

새 컨텍스트가 필요한지 판단한다.

**체크리스트:**
- [ ] 기존 컨텍스트의 언어로 설명할 수 없는 새로운 개념인가?
- [ ] 기존 컨텍스트와 독립적인 변경 주기를 가지는가?
- [ ] 기존 컨텍스트에 넣으면 해당 컨텍스트가 과도하게 커지는가?

### 2단계: Subdomain 분류

Core / Supporting / Generic 중 어디에 해당하는지 판단한다.

- **Core**: 직접 구축, 풍부한 도메인 모델, 높은 테스트 커버리지
- **Supporting**: 직접 구축하되 간결하게, 적정 수준의 복잡도
- **Generic**: SaaS/라이브러리 우선 검토, 꼭 필요한 경우만 직접 구축

### 3단계: Context Map 관계 정의

새 컨텍스트가 기존 컨텍스트와 어떻게 소통하는지 정의한다.

**질문:**
- 이 컨텍스트는 어떤 이벤트를 발행하는가?
- 이 컨텍스트는 어떤 이벤트를 구독하는가?
- 외부 시스템과의 통합이 필요한가? → ACL 필요 여부

### 4단계: 패키지 구조 생성

```
domain/{context-name}/
├── {subdomain}/
│   ├── api/
│   │   ├── {Subdomain}Api.java
│   │   ├── {Subdomain}QueryApi.java
│   │   └── dto/
│   ├── application/
│   │   ├── {Subdomain}Service.java
│   │   ├── command/
│   │   └── usecase/
│   ├── domain/
│   │   ├── {Subdomain}.java          # Aggregate Root
│   │   ├── {Subdomain}Repository.java
│   │   ├── vo/
│   │   └── event/
│   └── infrastructure/
│       └── JOOQ{Subdomain}Repository.java
```

### 5단계: 도메인 컨텍스트 문서 작성

`docs/domains/{context-name}-context.md`에 다음을 문서화한다:

```markdown
# {Context Name} Domain Context

## 개요
- 이 컨텍스트의 책임과 범위

## Aggregate Root
- 주요 엔티티, Value Object, 비즈니스 규칙

## Domain Events
- 발행하는 이벤트 목록과 발행 시점

## Context Map
- 의존하는/의존받는 다른 컨텍스트와의 관계

## 비즈니스 규칙
- 핵심 불변식(invariant)과 제약 조건
```

---

## 기존 컨텍스트에 Subdomain 추가

기존 Bounded Context 내에 새 subdomain을 추가할 때:

```
domain/club/
├── club/          # 기존
├── schedule/      # 기존
├── chat/          # 기존
├── feed/          # 기존
├── recruitment/   # 기존
├── sport/         # 기존
├── stats/         # 기존
└── dues/          # ← 새 subdomain 추가
```

**판단 기준:** "이 기능이 클럽 없이 존재할 수 있는가?"
- 아니오 → 기존 Club 컨텍스트의 subdomain으로 추가
- 예 → 별도 Bounded Context 검토

---

## Bounded Context와 배포 단위

현재 이 프로젝트는 **모놀리스**로, 모든 Bounded Context가 하나의 애플리케이션에 존재한다.

**모놀리스에서의 경계 유지 방법:**
1. 패키지 경계로 물리적 분리 (`domain/auth/`, `domain/club/`)
2. 컨텍스트 간 직접 호출 금지 — Domain Event로만 소통
3. 공유 데이터는 `global/` 패키지로 제한
4. 각 컨텍스트가 자체 Repository를 가짐 (다른 컨텍스트의 테이블 직접 접근 금지)

**마이크로서비스 전환 시:**
- 이벤트 기반 통합이 이미 되어 있으므로, 컨텍스트 단위로 분리 가능
- Generic subdomain (Auth, Notification)이 1순위 분리 후보
- Core subdomain (Club)은 가장 마지막에 분리 (리스크 최소화)
