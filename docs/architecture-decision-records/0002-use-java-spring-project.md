# 2. Use Java and Spring Project

Date: 2025-08-23

## 상태 Status

Accepted

## 배경 Context

현재 진행 중인 웹 애플리케이션 백엔드 시스템은 복잡한 비즈니스 로직과 높은 동시성 처리를 요구합니다. 안정적이고 확장 가능하며, 장기적인 유지보수가 용이한 기술 스택을 선정할 필요가 있습니다. 팀 내 다수의 개발자가 Java에 대한 숙련도를 가지고 있으며, 최신 JDK 버전이 제공하는 성능 및 생산성 향상 기능들을 적극적으로 활용하고자 합니다.

The current web application backend system requires complex business logic and high concurrency processing. It is necessary to select a stable, scalable, and easily maintainable technology stack for the long term. Many developers on the team are proficient in Java and want to actively utilize the performance and productivity improvements offered by the latest JDK versions.

## 결정 Decision

백엔드 기술 스택으로 Java 24 LTS와 Spring Boot 프레임워크를 사용하기로 결정합니다. Java는 강력한 성능, 안정성, 풍부한 생태계를 제공하며, 특히 JDK 24의 가상 스레드는 높은 동시성 처리에 유리합니다. Spring Boot는 빠른 개발과 쉬운 설정, 통합된 생태계를 통해 생산성을 높여줍니다. 팀의 Java 숙련도를 고려할 때, 이는 프로젝트 요구사항에 가장 적합한 선택입니다.

We decided to use Java 24 LTS and the Spring Boot framework for our backend technology stack. Java offers robust performance, stability, and a rich ecosystem. Specifically, JDK 24's virtual threads are advantageous for high concurrency. Spring Boot enhances productivity with rapid development, easy configuration, and an integrated ecosystem. Considering the team's proficiency in Java, this is the most suitable choice for the project's requirements.

## 결과 Consequences

- 안정적이고 확장 가능한 아키텍처를 구축합니다.
- 팀의 기존 Java 숙련도를 활용하여 개발 속도와 생산성을 높입니다.
- 가상 스레드를 사용하여 높은 동시성 요청을 효율적으로 처리합니다.
- 풍부한 생태계와 안정적인 지원을 통해 장기적인 유지보수가 용이해집니다.
- We will build a stable and scalable architecture.
- Increase development speed and productivity by leveraging the team's existing Java proficiency.
- Efficiently handle high concurrency requests using virtual threads.
- Long-term maintenance becomes easier through the rich ecosystem and stable support.