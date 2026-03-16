# 4. Use Domain-Driven Design

Date: 2025-08-23

## 상태 Status

Accepted

## 배경 Context

복잡한 비즈니스 도메인을 소프트웨어로 정확하게 모델링하고, 모든 팀원이 동일한 이해를 공유하는 것은 어려운 과제입니다. 비즈니스 로직이 기술적인 코드에 얽매이면, 시스템의 유지보수성과 확장성이 저하될 수 있습니다. 특히 모듈러 모놀리스 아키텍처에서는 각 모듈의 경계를 명확하게 정의할 기준이 필요합니다.

Accurately modeling a complex business domain in software and ensuring all team members share the same understanding is a significant challenge. When business logic becomes entangled with technical code, the system's maintainability and scalability can degrade. Especially within a modular monolith architecture, a clear standard is needed to define the boundaries of each module.

## 결정 Decision

우리는 도메인 주도 설계(Domain-Driven Design, DDD)를 프로젝트의 핵심 설계 접근법으로 채택하기로 결정합니다. 전략적 설계를 통해 유비쿼터스 언어(Ubiquitous Language)를 정의하고, 비즈니스 도메인을 여러 개의 경계 컨텍스트(Bounded Contexts)로 분리합니다. 전술적 설계를 활용하여 각 컨텍스트 내부를 애그리거트(Aggregates), 엔티티(Entities), 값 객체(Value Objects) 등으로 모델링하여 도메인 로직을 구현합니다.

We have decided to adopt Domain-Driven Design (DDD) as the core design approach for this project. Through strategic design, we will define a Ubiquitous Language and separate the business domain into multiple Bounded Contexts. Using tactical design, we will model the internals of each context with Aggregates, Entities, and Value Objects to implement the domain logic.

## 결과 Consequences

- 비즈니스 도메인과 소프트웨어 모델 간의 간극이 줄어들어, 요구사항 변경에 유연하게 대처할 수 있습니다.
- 유비쿼터스 언어를 통해 개발자와 비즈니스 전문가 간의 의사소통이 원활해집니다.
- 경계 컨텍스트는 모듈러 모놀리스의 모듈 경계를 결정하는 명확한 기준이 됩니다.
- DDD의 개념과 패턴에 대한 학습 곡선이 존재하며, 도메인 전문가와의 긴밀한 협업이 필수적입니다.
- The gap between the business domain and the software model is reduced, allowing for more flexible responses to changing requirements.
- Communication between developers and business experts is improved through the Ubiquitous Language.
- Bounded Contexts provide a clear basis for determining the module boundaries in the modular monolith.
- There is a learning curve for the concepts and patterns of DDD, and close collaboration with domain experts is essential.
