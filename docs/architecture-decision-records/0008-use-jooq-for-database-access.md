# 8. Use jOOQ for Database Access

Date: 2025-08-23

## 상태 Status

Accepted

## 배경 Context

데이터베이스와 상호작용할 데이터 접근 기술을 선택해야 합니다. Spring 생태계의 표준은 Spring Data JPA(Hibernate)이지만, ORM은 복잡성, 성능 문제, SQL 기능 제한 등의 단점을 가집니다. 예를 들어, JPA는 N+1 쿼리 문제처럼 예측하기 어려운 동작을 할 수 있고, 복잡한 쿼리 작성에 어려움이 있으며, 객체와 관계형 데이터 모델 간의 '임피던스 불일치' 문제가 발생합니다. 우리는 SQL의 모든 기능을 활용하면서도 타입 안정성을 보장하는 대안을 고려할 필요가 있습니다.

We need to select a data access technology to interact with our database. While Spring Data JPA (Hibernate) is the standard in the Spring ecosystem, ORMs have drawbacks such as complexity, performance issues, and limited SQL capabilities. For example, JPA can lead to unpredictable behavior like the N+1 query problem, makes writing complex queries difficult, and suffers from the object-relational impedance mismatch. We need to consider an alternative that offers the full power of SQL while ensuring type safety.

## 결정 Decision

우리는 데이터베이스 접근 계층에 jOOQ(Java Object Oriented Querying)를 사용하기로 결정합니다. jOOQ는 데이터베이스 스키마로부터 Java 클래스를 생성하여, Java 코드로 타입-세이프한 SQL 쿼리를 작성할 수 있게 해주는 라이브러리입니다. 이는 SQL을 추상화하는 대신, SQL을 중심으로 개발할 수 있게 하여 SQL의 모든 기능을 활용할 수 있게 합니다. JPA의 영속성 컨텍스트와 같은 복잡한 추상화 없이, 명시적이고 예측 가능한 데이터베이스 작업을 수행합니다.

We have decided to use jOOQ (Java Object Oriented Querying) for our data access layer. jOOQ is a library that generates Java classes from the database schema, allowing us to write type-safe SQL queries in Java. Instead of abstracting SQL away, it embraces it, enabling us to leverage the full power of SQL. It allows for explicit and predictable database operations without complex abstractions like JPA's persistence context.

## 결과 Consequences

- **긍정적:**
- **타입-세이프 SQL:** 컴파일 시점에 SQL 구문 오류를 방지하여 런타임 에러를 줄입니다.
- **완벽한 SQL 제어:** 데이터베이스 벤더별 함수나 복잡한 조인 등 SQL의 모든 기능을 사용할 수 있습니다.
- **투명성 및 성능:** 숨겨진 동작이 없어 쿼리 성능을 예측하고 최적화하기 쉽습니다.
- **리팩토링 용이성:** 데이터베이스 스키마 변경이 Java 코드에 반영되어 유지보수성이 향상됩니다.

- **고려사항:**
- **보일러플레이트:** 간단한 CRUD 작업은 Spring Data JPA의 레포지토리 인터페이스에 비해 코드가 더 길어질 수 있습니다.
- **빌드 복잡성:** 빌드 과정에 데이터베이스 스키마와 Java 코드를 동기화하기 위한 코드 생성 단계가 필요합니다.
- **학습 곡선:** JPA에 익숙한 개발자는 jOOQ API와 SQL 중심의 개발 방식에 적응할 시간이 필요합니다.

- **Positive:**
- **Type-Safe SQL:** Prevents SQL syntax errors at compile time, reducing runtime failures.
- **Full SQL Control:** Allows the use of all SQL features, including vendor-specific functions and complex joins.
- **Transparency & Performance:** No hidden magic makes it easier to reason about and optimize query performance.
- **Refactoring Safety:** Database schema changes are reflected in the generated Java code, improving maintainability.

- **Considerations:**
- **Boilerplate:** Simple CRUD operations can be more verbose compared to Spring Data JPA's repository interfaces.
- **Build Complexity:** Requires a code generation step in the build process to keep the Java code in sync with the database schema.
- **Learning Curve:** Developers familiar with JPA will need time to adapt to the jOOQ API and a more SQL-centric approach.
