# 케이스 스터디 — 검증 → 동의 시나리오

## 요구사항 (예시)

두 기능이 있다.

- **부모-자녀 검증(Verification)**: 부모가 단독으로 수행 가능. 수기 검토가 필요하면 **7일** 동안 서류를 확인.
- **부모-자녀 동의(Consent)**: 자녀가 부모에게 요청 → 부모가 승인. 기본 만료 **24시간**.

규칙:
1. **동의는 검증 이력이 있어야 진행 가능**. 없으면 "검증 → 동의" 순서.
2. **검증이 수기 검토 절차에 들어가면 동의 만료를 7일 연장**.

## 설계 결정

### 1) Aggregate 경계 — 별도 AR

| AR | 이유 |
|----|------|
| `Verification` | 단독 발행 가능, 자체 라이프사이클 (SUBMITTED → MANUAL_REVIEW → APPROVED/REJECTED) |
| `Consent` | Verification 전제로 하지만 별도 라이프사이클 (PENDING → APPROVED/REJECTED/EXPIRED) |

하나의 AR로 묶으면 "검증만 있고 동의는 없음" 상태에서 절반이 null로 비는 빈약한 모델이 된다.

### 2) 검증 이력 invariant — 타입으로 강제

`Consent` 팩토리에서 `Verification`을 인자로 받게 한다. Verification 없는 Consent 생성 자체가 불가능해진다.

```java
public class Consent extends AggregateRoot {
    public static Consent request(Verification v, String childId, Purpose p) {
        if (!v.covers(childId)) throw new IllegalArgumentException();
        return new Consent(..., v.id(), ...);
    }
}
```

## "검증 → 동의" 플로우 구현

### ❌ 이벤트 체인으로 엮으면 안 됨

```
Consent 요청 → ConsentRequestedEvent → VerificationHandler가 만들고...
```

same-tx가 필요 → `BEFORE_COMMIT` 필요 → 앞 문서에서 다룬 안티패턴.

### ✅ Application Service 오케스트레이션

```java
// application/ — RequestParentChildConsentService
@Transactional
public Consent request(RequestConsentCommand cmd) {
    Verification verification = verificationRepo
        .findByParentAndChild(cmd.parentId(), cmd.childId())
        .orElseGet(() -> verifyParentChildUseCase
            .verify(new VerifyCommand(cmd.parentId(), cmd.childId(), cmd.evidence())));

    Consent consent = Consent.request(verification, cmd.childId(), cmd.purpose());
    return consentRepo.save(consent);
}
```

**정당화**:

| 질문 | 답 |
|------|----|
| 두 AR이 한 tx에 묶여도 되는가? | 네. "생성 체인"은 Aggregate 원칙의 허용된 예외. Verification 없는 Consent가 존재할 수 없으므로 원자성이 **의미적으로** 요구됨 |
| Domain Event는? | 발행하되 **후속 처리용만** (감사 로그, 알림). 플로우 제어엔 안 씀 |
| 검증 단독 API는? | 별도 `VerifyParentChildApi` → `VerifyParentChildUseCase`. Consent 플로우는 이 UseCase를 **재호출** |

## "수기 검토 진입 시 동의 만료 +7일"

### 이 부분이 이벤트 적합 시나리오

| 체크 | 판정 |
|------|------|
| Cross-aggregate? | ✅ Verification → Consent |
| Eventual consistency 허용? | ✅ 몇 초 지연 무해 |
| same-tx 강제 필요? | ❌ 실패해도 각자 살아남아 OK |
| AR 책임 분리 유지? | ✅ Consent는 "검증 수기 검토"를 모름 |

→ **이벤트 + `application/` consumer** 구조 적합.

## 함정 5가지

### 함정 1: 발행 타이밍 — "적재 시"가 아니라 "상태 전이 시"

```java
// ❌ VerificationSavedEvent — 모든 저장에 튀어나옴
// ✅ VerificationEnteredManualReviewEvent — 비즈니스 의미 있는 전이
public class Verification extends AggregateRoot {
    public void enterManualReview() {
        if (this.status != SUBMITTED) throw ...;
        this.status = MANUAL_REVIEW;
        this.manualReviewStartedAt = now;
        addEvent(new VerificationEnteredManualReviewEvent(
            id, parentId, childId, now));
    }
}
```

자동 검증 통과는 수기 검토 불필요 → 이 이벤트가 발행 안 됨. Consumer도 깔끔.

### 함정 2: Consent 쪽 invariant + 멱등성

```java
public void extendForManualReview(Duration extension) {
    if (this.status != PENDING)
        throw new IllegalStateException("Cannot extend non-pending consent");
    if (this.manualReviewExtensionApplied)
        return;  // 멱등
    this.expiresAt = this.expiresAt.plus(extension);
    this.manualReviewExtensionApplied = true;
}
```

**멱등성이 중요한 이유**: 현재는 at-most-once처럼 보여도 outbox 도입 시 at-least-once가 된다. 처음부터 멱등으로 설계하는 것이 안전.

### 함정 3: 이벤트 페이로드 — AR 간 참조 방향

Verification이 `consentId`를 아는 건 경계를 흐린다. 보통 **Verification은 독립**.

```java
// ✅ 추천: 공통 키만 담고 Consumer가 Consent 탐색
record VerificationEnteredManualReviewEvent(
    String verificationId, String parentId, String childId, Instant at
) implements DomainEvent {}

// Consumer
Consent consent = consentRepo.findPendingBy(e.parentId(), e.childId());
if (consent != null) consent.extendForManualReview(Duration.ofDays(7));
```

"(parent, child)당 특정 시점 PENDING Consent는 유일" invariant를 유지하면 lookup 충돌 없음.

### 함정 4: Outbox 1순위 후보

- 유실 시 Consent가 24h에 만료 → 사용자가 "검토 중이라더니 동의가 사라졌어요" 컴플레인
- 수기 검토는 며칠 걸리므로 **운영자가 못 알아챈다** (FCM처럼 즉시 피드백 없음)
- `AFTER_COMMIT + @Async`는 앱 재시작·consumer 예외 시 조용히 유실

→ [Outbox 문서](03-outbox-consideration.md) 2순위로 포함.

### 함정 5: 역방향 케이스 누락

페어로 설계해두지 않으면 고아 Consent가 생긴다.

| 전이 | 이벤트 | Consent 처리 |
|------|--------|------------|
| SUBMITTED → MANUAL_REVIEW | `VerificationEnteredManualReviewEvent` | `expiresAt +7d` |
| MANUAL_REVIEW → APPROVED | `VerificationApprovedEvent` | 상태 유지 or 자동 PROCEED |
| MANUAL_REVIEW → REJECTED | `VerificationRejectedEvent` | Consent 자동 REJECTED |
| MANUAL_REVIEW 완료 but Consent 이미 EXPIRED | — | no-op, 로그만 |

## 최종 구조 제안

```
domain/{ctx}/parent-child/
├── verification/
│   ├── api/              VerifyParentChildApi
│   ├── application/
│   │   └── VerifyParentChildService
│   └── domain/
│       ├── Verification (AR)
│       └── event/
│           ├── VerificationEnteredManualReviewEvent
│           ├── VerificationApprovedEvent
│           └── VerificationRejectedEvent
└── consent/
    ├── api/
    │   ├── RequestConsentApi
    │   └── ApproveConsentApi
    ├── application/
    │   ├── RequestParentChildConsentService   // Verification 조율
    │   ├── ApproveConsentService
    │   └── ConsentVerificationEventHandler    // 이벤트 구독
    └── domain/
        └── Consent (AR, verificationId 참조)
```

## 시나리오별 진입점

| 시나리오 | 진입점 | 트랜잭션 |
|---------|--------|---------|
| 부모 단독 검증 | `POST /verifications` → `VerifyParentChildUseCase` | tx 1 |
| 자녀 요청 + 검증 이력 있음 | `POST /consents/request` → `Consent.request()` | tx 1 |
| 자녀 요청 + 검증 이력 없음 | 같은 엔드포인트 → 내부에서 Verification 선행 → Consent | tx 1 (둘 다 묶임) |
| 부모 동의 | `POST /consents/{id}/approve` → `ApproveConsentService` | tx 1 |
| 수기 검토 진입 | (비동기) `VerificationEnteredManualReviewEvent` → Consumer가 Consent 연장 | tx 2 (분리) |

## 요약 규칙

1. **이벤트 선택 자체는 적합** (cross-aggregate + eventual OK).
2. **발행 시점은 "저장"이 아니라 "상태 전이"** — 이벤트 이름을 비즈니스 의미로.
3. **Consent 연장 메서드는 invariant + 멱등**.
4. **Outbox 도입 1순위 후보** — 유실 시 UX 직격탄이고 늦게 드러남.
5. **REJECTED/APPROVED 역방향 이벤트도 같이 설계** — 아니면 고아 Consent.
6. **"검증 → 동의" 시퀀스는 이벤트로 엮지 말 것** — Application Service 오케스트레이션.
