# Auth Domain Context

인증/인가 관련 Bounded Context

## 서브 도메인

### 1. Admin (관리자)
- **Aggregate Root**: `Admin`
- **역할**: 관리자 계정 관리
- **주요 기능**:
  - 관리자 등록/로그인
  - 비밀번호 암호화 (BCrypt)
  - Role 기반 권한 ("BASIC")

```
Admin
├── id: String
├── userId: String (SignUp과 연결)
├── password: String (암호화)
└── role: String
```

### 2. OIDC (소셜 로그인)
- **Aggregate Root**: `Oidc`
- **역할**: OAuth2/OIDC 소셜 로그인 처리
- **지원 Provider**: `OidcProviders` enum

```
Oidc
├── id: String
├── provider: OidcProviders
├── providerUserId: String
└── userId: String
```

### 3. SignIn (로그인/세션)
- **Aggregate Root**: `SignIn`
- **역할**: 로그인 기록 및 세션 관리
- **관련 객체**:
  - `SignInSession`: HTTP 세션에 저장되는 로그인 정보
  - `SignInToken`: 인증 토큰

```
SignIn
├── id: String
├── userId: String
├── signInType: String
├── token: String
└── createdAt: LocalDateTime

SignInSession (세션 저장용)
├── userId: String
└── role: String
```

### 4. SignUp (회원가입)
- **Aggregate Root**: `SignUp`
- **역할**: 신규 사용자 ID 발급
- **특징**: Users 도메인과 분리하여 인증 책임 분리

```
SignUp
├── id: String
└── createdAt: LocalDateTime
```

## 도메인 이벤트
- (현재 정의된 이벤트 없음)

## 의존 관계
- `SignUp` → `Users`: 회원가입 시 Users 생성
- `Admin` → `SignUp`: 관리자 생성 시 userId 할당
- `Oidc` → `SignUp`: 소셜 로그인 시 userId 연결
