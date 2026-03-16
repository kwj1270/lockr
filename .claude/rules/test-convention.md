---
paths:
  - "src/test/java/**/*.java"
---

# Test Convention

## 테스트 클래스 네이밍
- Domain Test: `{Entity}Test` (예: `ScheduleTest`, `ClubTest`)
- Service Test: `{Service}Test` (예: `ScheduleServiceTest`)
- Repository Test: `{Repository}Test` (예: `ScheduleRepositoryTest`)
- API Test: `{Api}Test` (예: `SignInAdminApiTest`)

## 테스트 메서드 네이밍
- 정상 케이스: `should{동작}` (예: `shouldCreateSchedule`)
- 예외 케이스: `shouldThrowWhen{조건}` (예: `shouldThrowWhenScheduleTimeInPast`)
- 한 메서드 = 한 가지 행동만 테스트

## InMemory Repository 패턴

Domain/Service 테스트에서는 실제 DB 대신 InMemory Repository를 사용한다.

구현 규칙:
- `HashMap<String, Entity>` 기반 저장소
- `deepCopy()` 메서드로 저장 시 복사본 저장 (참조 공유 방지)
- `clear()` 메서드 제공 (테스트 간 격리)
- `reconstruct()` 팩토리 메서드로 깊은 복사 수행

참고 구현:
- `test/.../schedule/infrastructure/InMemoryScheduleRepository.java`
- `test/.../notification/infrastructure/InMemoryNotificationRepository.java`

## 테스트 구조
- Given-When-Then 패턴 사용
- @BeforeEach에서 InMemory Repository 초기화
- 테스트 간 상태 공유 금지
