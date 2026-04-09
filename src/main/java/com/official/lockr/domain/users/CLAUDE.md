# Users Bounded Context

사용자 프로필 도메인. 추가정보 관리, 회원 탈퇴.

## Aggregate

| 타입 | 이름 | 핵심 역할 |
|------|------|----------|
| AR | Users | 프로필 관리, 탈퇴 처리, WithdrawnUserEvent 발행 |

## Domain Events

| Event | 설명 |
|-------|------|
| WithdrawnUserEvent | 탈퇴 시 발행 → auth 도메인에서 소비 |

## Value Objects

- **UserAdditionalInfo**: name, birthDate, phone, gender, profileImage

## Cross-Domain Dependencies

| 외부 도메인 | 방향 | Event | 설명 |
|------------|------|-------|------|
| auth | users → auth | WithdrawnUserEvent | OIDC + SignIn 토큰 삭제 |

## 비즈니스 규칙

| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| withdraw | 이미 탈퇴한 회원은 재탈퇴 불가 | IllegalStateException |

## Gotchas

- Soft delete: deletedAt 타임스탬프 방식
- 탈퇴 시 WithdrawnUserEvent로 auth 도메인 연쇄 정리
