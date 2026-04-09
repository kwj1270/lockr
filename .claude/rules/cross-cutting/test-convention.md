---
globs:
  - "src/test/java/**/*.java"
  - "src/test/resources/features/**/*.feature"
description: "테스트, InMemory, Cucumber, BDD, feature, 시나리오, 단위테스트"
---

## 테스트 컨벤션

### InMemory Repository
- Domain Repository 인터페이스를 구현하는 테스트용 구현체
- `HashMap<String, T>` 기반, `deepCopy()`로 불변성 보장
- 위치: `src/test/java/.../infrastructure/InMemory{Name}Repository.java`
- `clear()` 메서드 제공 (테스트 간 격리)

### Cucumber BDD
- Feature 파일 언어: `# language: ko`
- 위치: `src/test/resources/features/{subdomain}.feature`
- 구조: `기능` → `배경` (공통 setup) → `시나리오` (개별 케이스)
- 키워드: `먼저`, `그리고`, `만약`, `그러면`
- 도메인별 1개 feature 파일 원칙 (subdomain 단위)

### 도메인 단위 테스트
- 위치: `src/test/java/.../domain/{Name}Test.java`
- InMemory Repository 사용, Spring 컨텍스트 없이 순수 도메인 로직 검증
- 메서드명: `should{행위}` 패턴 (e.g., `shouldCreateSchedule`)
