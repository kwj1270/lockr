# Auth Domain Context

인증/인가 관련 Bounded Context

## 서브 도메인

### 1. Admin (관리자)
- **Aggregate Root**: `Admin`
- **역할**: 관리자 계정 관리
- **주요 기능**:
  - 관리자 등록/로그인
  - 비밀번호 평문 비교 (`matchPassword`)
  - Role 기반 권한 ("BASIC")

```
Admin
├── id: String
├── userId: String (nullable, Users 도메인과 연결)
├── password: String
├── role: String
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime (nullable)
```

**주요 메서드**:
- `Admin.init(id, userId, password)` - 관리자 생성 (role = "BASIC")
- `hasNotUserId()` - userId 미설정 여부 확인
- `matchPassword(rawPassword)` - 비밀번호 일치 확인

---

### 2. OIDC (소셜 로그인)
- **Aggregate Root**: `Oidc`
- **역할**: OAuth2/OIDC 소셜 로그인 처리
- **Provider 타입**: `ProviderType` enum (GOOGLE, APPLE)
- **인터페이스**: `OidcProviders` - 외부 Provider로부터 identifier를 가져오는 포트

```
Oidc
├── id: String
├── userId: String (nullable, 최초 로그인 시 미설정)
├── provider: ProviderType  (GOOGLE | APPLE)
├── identifier: String      (Provider 고유 사용자 식별자)
├── metadata: String (nullable)
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime (nullable)
```

**용어 정리**:
- `OidcProviders`: 외부 OIDC Provider와 통신하는 **도메인 인터페이스** (포트). `HttpOidcProviders`가 구현체.
  - `identifier(idToken, providerType)` - idToken을 검증하고 Provider의 사용자 식별자를 반환
- `ProviderType`: 지원하는 Provider 종류를 나타내는 **enum** (GOOGLE, APPLE)

**주요 메서드**:
- `Oidc.init(provider, identifier)` - 신규 OIDC 레코드 생성 (userId는 null)
- `hasNotUserId()` - userId 미설정 여부 확인 (신규 회원 판별에 사용)
- `setUserId(userId)` - userId 연결 (신규 회원 가입 시 호출)

---

### 3. SignIn (로그인/세션)
- **Aggregate Root**: `SignIn`
- **역할**: 로그인 기록 및 세션/토큰 관리
- **관련 객체**:
  - `SignInToken`: 장기 인증 토큰 (별도 도메인 엔티티)
  - `SignInSession`: HTTP 세션에 저장되는 로그인 상태 (Value Object)

```
SignIn                              (Aggregate Root)
├── id: String
├── userId: String
├── deviceId: String
├── deviceName: String
├── deviceOS: String
├── ipAddress: String
├── userAgent: String
└── createdAt: LocalDateTime

SignInToken                         (도메인 엔티티)
├── id: String
├── userId: String
├── signInId: String                (SignIn과 연결)
├── token: String                   (UUID)
├── expiresAt: LocalDateTime        (생성 시 +30일)
├── createdAt: LocalDateTime
└── deletedAt: LocalDateTime (nullable, 취소 시 설정)

SignInSession                       (Value Object, HTTP 세션 저장용)
├── userId: String
├── deviceId: String
├── deviceName: String
├── deviceOS: String
├── ipAddress: String
├── userAgent: String
└── createdAt: LocalDateTime
```

**SignInToken 주요 메서드**:
- `SignInToken.init(userId, signInId)` - 토큰 생성 (UUID 발급, 30일 유효)
- `revoke()` - 토큰 취소 (deletedAt 설정)
- `isExpired()` - 만료 여부 확인
- `isRevoked()` - 취소 여부 확인
- `isValid()` - 유효 여부 확인 (`!isExpired() && !isRevoked()`)

**SignInSession 생성 방법**:
- `SignInSession.from(SignIn)` - SignIn 도메인 객체로부터 생성
- `SignInSession.from(SignInToken, HttpHeaderContext)` - 토큰 기반 자동 로그인 시 생성

---

## 도메인 이벤트

### ProcessedSignInEvent
- **발행 시점**: `SignIn.init()` 호출 시 (로그인 기록 저장 직후)
- **역할**: SignIn 완료 후 SignInToken 자동 발급을 트리거
- **소비자**: `SignInTokenConsumer` (BEFORE_COMMIT)

```
ProcessedSignInEvent
├── id: String          (SignIn.id)
├── userId: String
├── deviceId: String
├── deviceName: String
├── deviceOS: String
├── ipAddress: String
├── userAgent: String
└── createdAt: LocalDateTime
```

**이벤트 흐름**:
```
SignIn.init()
  → ProcessedSignInEvent 발행
    → SignInTokenConsumer.consume()
      → RegisterSignInTokenUseCase.register()
        → SignInToken 생성 및 저장
```

---

## 이벤트 소비자 (Event Consumer)

### SignInTokenConsumer
- **위치**: `signin/api/SignInTokenConsumer`
- **트리거**: `ProcessedSignInEvent` (BEFORE_COMMIT)
- **역할**: 로그인 완료 시 `SignInToken` 자동 발급
- **재시도**: `RetryTemplate` 사용

### WithdrawnUserEventConsumer
- **위치**: `signin/api/WithdrawnUserEventConsumer`
- **트리거**: `WithdrawnUserEvent` (Users 도메인 발행, BEFORE_COMMIT)
- **역할**: 회원 탈퇴 시 해당 사용자의 OIDC 정보 및 SignInToken 일괄 삭제
- **재시도**: `RetryTemplate` 사용

```
WithdrawnUserEvent (Users 도메인)
  → WithdrawnUserEventConsumer.consume()
    → OidcRepository.deleteByUserId(userId)
    → SignInTokenRepository.deleteByUserId(userId)
```

---

## 의존 관계
- `Admin` → Users 도메인: userId를 통해 연결 (직접 의존 없음)
- `Oidc` → Users 도메인: userId를 통해 연결 (직접 의존 없음)
- `SignIn` → `SignInToken`: `ProcessedSignInEvent`를 통해 비동기 연결
- `WithdrawnUserEventConsumer` → `OidcRepository`, `SignInTokenRepository`: 회원 탈퇴 시 인증 데이터 정리

## SignUp 도메인에 대하여
코드상 별도의 `SignUp` 도메인은 존재하지 않는다. 신규 사용자 등록은 Users 도메인에서 처리되며, OIDC 로그인 시 `Oidc.hasNotUserId()`로 신규 회원을 판별하여 Users 도메인에서 사용자를 생성한다.
