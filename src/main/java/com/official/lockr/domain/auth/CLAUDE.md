# Auth Bounded Context

인증/인가 도메인. Admin 로그인, OIDC (카카오), 세션 토큰 관리.

## Aggregate & VO

| 타입 | 이름 | 핵심 역할 |
|------|------|----------|
| AR | SignIn | 로그인 세션 추적, ProcessedSignInEvent 발행 |
| VO | SignInToken | 30일 TTL, isValid = !isExpired && !isRevoked, revoke() |
| VO | Admin | 관리자 인증, userId 지연 바인딩 (생성 시 null 가능) |
| VO | Oidc | OIDC 제공자 연동, userId 지연 바인딩, setUserId()로 후속 연결 |

## Domain Events

| Event | 설명 |
|-------|------|
| ProcessedSignInEvent | 로그인 세션 생성 시 발행 |

## Cross-Domain Dependencies

| 외부 도메인 | 방향 | Event | 설명 |
|------------|------|-------|------|
| users | users → auth | WithdrawnUserEvent | 탈퇴 시 OIDC + SignIn 토큰 삭제 |

## Gotchas

- Admin/Oidc의 userId는 지연 바인딩 — 생성 시 null, 이후 setUserId()로 연결
- 모든 auth 엔티티는 deletedAt 타임스탬프로 soft delete
- SignInToken 만료 판정: 생성일 + 30일

## 비즈니스 규칙

| 대상 | 규칙 |
|------|------|
| SignInToken.isExpired() | 생성일 + 30일 초과 시 만료 |
| SignInToken.isValid() | !isExpired && !isRevoked |
| SignInToken.revoke() | deletedAt 타임스탬프 설정 |
| Admin.hasNotUserId() | userId null 허용 (지연 바인딩) |
| Oidc.setUserId() | userId 후속 연결, updatedAt 갱신 |
