# 3. Use Modular Monolith Architecture

Date: 2025-08-23

## 상태 Status

Accepted

## 배경 Context

프로젝트 초기 단계에서는 빠른 개발 속도와 배포의 단순성이 중요합니다. 하지만 향후 시스템이 성장함에 따라 확장성과 유지보수성을 확보해야 하는 과제도 있습니다. 마이크로서비스 아키텍처는 복잡성이 높고 초기 개발 비용이 많이 드는 반면, 전통적인 모놀리스 아키텍처는 모듈 간 결합도가 높아져 장기적으로 유지보수가 어려워질 수 있습니다.

In the early stages of a project, rapid development speed and simplicity of deployment are important. However, as the system grows, it also faces the challenge of ensuring scalability and maintainability. While a microservices architecture has high complexity and initial development costs, a traditional monolithic architecture can become difficult to maintain in the long run due to high coupling between modules.

## 결정 Decision

우리는 모듈러 모놀리스(Modular Monolith) 아키텍처를 채택하기로 결정했습니다. 이 아키텍처는 전체 애플리케이션을 하나의 단위로 배포하는 모놀리식 접근 방식을 유지하면서, 내부적으로는 비즈니스 도메인을 기준으로 독립적인 모듈로 코드를 구성합니다. 각 모듈은 명확한 API를 통해 통신하며, 모듈 간의 직접적인 종속성은 최소화합니다.

We have decided to adopt a Modular Monolith architecture. This architecture maintains the monolithic approach of deploying the entire application as a single unit, while internally organizing the code into independent modules based on business domains. Each module communicates through a well-defined API, minimizing direct dependencies between modules.

## 결과 Consequences

- 개발 초기에는 단일 코드베이스와 배포 파이프라인을 통해 개발 및 운영의 복잡성을 낮춥니다.
- 비즈니스 도메인에 따라 코드가 명확하게 분리되어 응집도는 높이고 결합도는 낮춥니다.
- 향후 시스템 규모가 커질 경우, 각 모듈을 독립적인 마이크로서비스로 전환하기 용이한 구조를 가집니다.
- 모듈 간 경계를 엄격하게 유지하기 위한 개발팀의 규율이 요구됩니다.
- Reduces development and operational complexity in the early stages with a single codebase and deployment pipeline.
- Code is clearly separated by business domain, increasing cohesion and reducing coupling.
- Provides a structure that makes it easier to transition individual modules into independent microservices if the system needs to scale in the future.
- Requires discipline from the development team to strictly maintain module boundaries.
