# 10. 인수 테스트 전략 정의 (Define Acceptance Testing Strategy)

Date: 2025-08-29

## 상태 Status

Accepted

## 배경 Context

단위 테스트와 통합 테스트는 개별 컴포넌트와 컴포넌트 간의 상호작용을 검증하지만, 전체 시스템이 비즈니스 요구사항과 사용자 시나리오에 따라 올바르게 동작하는지 종단간(end-to-end)으로 확인하는 절차가 부족합니다. 기능이 사용자 관점에서 기대대로 작동하는지, 새로운 변경사항이 기존 기능에 영향을 미치지 않는지 보장할 수 있는 테스트 전략이 필요합니다.

While unit and integration tests verify individual components and their interactions, we lack a process to confirm that the entire system functions correctly from an end-to-end perspective according to business requirements and user scenarios. We need a testing strategy to ensure that features work as expected from a user's point of view and that new changes do not break existing functionality.

## 결정 Decision

공식적인 인수 테스트(Acceptance Test) 전략을 도입하기로 결정합니다. 인수 테스트는 API를 중심으로 사용자 시나리오를 검증하는 방식으로 구현합니다.

1.  **테스트 프레임워크:** Spring Boot의 `@SpringBootTest`를 사용하여 전체 애플리케이션 컨텍스트를 로드하고, 실제와 유사한 환경에서 테스트를 실행합니다.
2.  **API 클라이언트:** `RestAssured` 라이브러리를 사용하여 HTTP 요청을 보내고 응답을 검증합니다. `RestAssured`는 API 테스트를 위한 간결하고 가독성 높은 DSL(Domain-Specific Language)을 제공합니다.
3.  **테스트 구조:** 테스트는 사용자 스토리나 기능 단위로 구성합니다. 예를 들어, '사용자 회원가입' 기능에 대한 인수 테스트는 회원가입 API를 호출하고, 데이터베이스에 사용자가 정상적으로 생성되었는지, 응답은 예상대로 반환되는지 등을 검증합니다.
4.  **테스트 환경:** 모든 인수 테스트는 Testcontainers(ADR-0009)를 통해 생성된 격리된 실제 데이터베이스 환경에서 실행하여 일관성과 신뢰도를 높입니다.

We have decided to adopt a formal acceptance testing strategy. These tests will be implemented by validating user scenarios through our API.

1.  **Test Framework:** We will use Spring Boot's `@SpringBootTest` to load the entire application context and run tests in a realistic environment.
2.  **API Client:** We will use the `RestAssured` library to make HTTP requests and validate responses. `RestAssured` provides a concise and readable DSL (Domain-Specific Language) for API testing.
3.  **Test Structure:** Tests will be organized around user stories or features. For example, an acceptance test for the "User Sign-up" feature will call the sign-up API and then verify the outcomes, such as the user being correctly created in the database and the expected response being returned.
4.  **Test Environment:** All acceptance tests will run against an isolated, real database instance managed by Testcontainers (as per ADR-0009) to enhance consistency and reliability.

## 결과 Consequences

- **긍정적:**
- **높은 배포 신뢰도:** 기능이 종단간으로 동작함을 검증하여, 프로덕션 배포에 대한 높은 신뢰를 제공합니다.
- **살아있는 문서:** 인수 테스트 코드는 사용자 관점에서 시스템의 동작을 설명하는 실행 가능한 문서 역할을 합니다.
- **회귀 방지:** 중요한 사용자 워크플로우에서 발생하는 버그(회귀)를 효과적으로 방지하는 안전망이 됩니다.
- **구현과 테스트의 분리:** 테스트가 내부 구현이 아닌 API 명세(무엇을 하는지)에 집중하므로, 리팩토링 시 테스트 코드의 수정이 최소화됩니다.

- **고려사항:**
- **느린 실행 속도:** 인수 테스트는 전체 애플리케이션 구동, 네트워크 통신, 데이터베이스 상호작용을 포함하므로 단위/통합 테스트보다 본질적으로 느립니다.
- **관리 복잡성:** 종단간 테스트는 테스트 데이터 관리와 시스템 상태 유지가 복잡하여 작성 및 유지보수 비용이 높을 수 있습니다.
- **디버깅의 어려움:** 인수 테스트 실패 시, 실패의 원인이 되는 특정 컴포넌트를 찾아내기가 단위 테스트보다 어렵습니다.

- **Positive:**
- **High Deployment Confidence:** Validates that features work end-to-end, providing high confidence for production deployments.
- **Living Documentation:** Acceptance test code serves as executable documentation that describes the system's behavior from a user's perspective.
- **Regression Prevention:** Acts as a safety net that effectively prevents regressions in critical user workflows.
- **Decoupling from Implementation:** Tests focus on the API contract (the "what") rather than the internal implementation (the "how"), making them less brittle to refactoring.

- **Considerations:**
- **Slower Execution:** Acceptance tests are inherently slower than unit or integration tests as they involve starting the full application, network communication, and database interactions.
- **Maintenance Complexity:** Writing and maintaining end-to-end tests can be complex, requiring careful management of test data and system state.
- **Debugging Challenges:** When an acceptance test fails, it can be more difficult to pinpoint the specific component causing the failure compared to a unit test.
