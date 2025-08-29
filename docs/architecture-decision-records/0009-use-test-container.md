# 9. Use Testcontainers for Integration Testing

Date: 2025-08-29

## 상태 Status

Accepted

## 배경 Context

애플리케이션의 데이터베이스 연동 계층(예: jOOQ 리포지토리)에 대한 신뢰성 있는 통합 테스트 환경을 구축해야 합니다. 인메모리 데이터베이스(H2 등)는 빠르지만, 실제 운영 환경에서 사용하는 데이터베이스(MySQL)와 SQL 문법이나 기능 차이가 있어 테스트의 정확성이 떨어질 수 있습니다. 또한, 여러 테스트가 공유하는 개발용 데이터베이스를 사용하면 테스트 간 데이터 충돌로 인해 테스트가 불안정해지고(flaky), 테스트 환경을 일관되게 유지하기 어렵습니다. 우리는 각 테스트 실행을 위해 격리되고 일관되며 실제와 동일한 테스트 환경이 필요합니다.

We need to establish a reliable integration testing environment for our application's data access layer (e.g., jOOQ repositories). In-memory databases like H2 are fast, but their SQL dialects and features can differ from our production database (MySQL), potentially reducing test accuracy. Using a shared development database can lead to flaky tests due to data conflicts between tests and makes it difficult to maintain a consistent test environment. We require an isolated, consistent, and high-fidelity testing environment for each test run.

## 결정 Decision

우리는 통합 테스트를 위해 Testcontainers를 사용하기로 결정합니다. Testcontainers는 테스트 코드 내에서 프로그래밍 방식으로 도커 컨테이너의 생명주기를 관리할 수 있게 해주는 Java 라이브러리입니다. 통합 테스트 시 실제 MySQL 데이터베이스가 담긴 도커 컨테이너를 실행하여, 운영 환경과 동일한 환경에서 테스트를 진행할 수 있습니다. 각 테스트 또는 테스트 클래스 단위로 독립적인 데이터베이스 인스턴스를 생성하고 폐기함으로써 테스트 간 완벽한 격리를 보장합니다.

We have decided to use Testcontainers for our integration tests. Testcontainers is a Java library that allows us to programmatically manage the lifecycle of Docker containers from within our test code. For integration tests, we will spin up a Docker container running a real MySQL database, ensuring that our tests execute against an environment identical to production. By creating and destroying a fresh database instance for each test or test class, we guarantee complete isolation between tests.

## 결과 Consequences

- **긍정적:**
- **높은 신뢰도:** 운영 환경과 동일한 데이터베이스(MySQL)에서 테스트를 실행하므로, 인메모리 데이터베이스에서 발생하는 호환성 문제를 원천적으로 차단합니다.
- **테스트 격리:** 각 테스트는 일회용 데이터베이스 인스턴스 위에서 실행되므로, 데이터 충돌 없이 안정적이고 예측 가능한 테스트 결과를 보장합니다.
- **개발 편의성:** 개발자가 로컬에 직접 데이터베이스를 설치하고 관리할 필요 없이, 테스트 실행 시 자동으로 환경이 구성되고 정리됩니다.
- **CI/CD 통합:** 빌드 환경에 Docker만 설치되어 있다면 CI/CD 파이프라인에 쉽게 통합할 수 있습니다.

- **고려사항:**
- **테스트 실행 속도:** 인메모리 데이터베이스보다 도커 컨테이너를 시작하는 데 시간이 더 걸려 전체 테스트 스위트의 실행 시간이 늘어날 수 있습니다.
- **리소스 사용량:** 도커 컨테이너는 인메모리 데이터베이스보다 더 많은 시스템 리소스(CPU, 메모리)를 소모합니다.
- **도커 의존성:** 개발 및 빌드 환경에 도커가 설치 및 실행되어 있어야 하므로, 외부 환경에 대한 의존성이 추가됩니다.

- **Positive:**
- **High Fidelity:** Tests run against the same database system (MySQL) as in production, eliminating compatibility issues that can arise with in-memory databases.
- **Test Isolation:** Each test runs on a clean, ephemeral database instance, ensuring reliable and predictable results without data conflicts.
- **Developer Convenience:** Eliminates the need for developers to manually install and manage a local database; the environment is automatically set up and torn down.
- **CI/CD Integration:** Easily integrates into CI/CD pipelines, provided that Docker is available in the build environment.

- **Considerations:**
- **Performance:** Starting a Docker container takes longer than connecting to an in-memory database, which can increase the overall execution time of the test suite.
- **Resource Usage:** Running Docker containers consumes more system resources (CPU, memory) than in-memory databases.
- **Dependency on Docker:** The development and build environments must have Docker installed and running, adding an external dependency to the setup.
