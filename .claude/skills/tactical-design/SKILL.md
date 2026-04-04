---
name: tactical-design
description: DDD 전술적 설계 패턴 가이드. Spring Boot + jOOQ 기반의 도메인별 패키징, Simplified CQRS, AggregateRoot, Domain Event, UseCase 인터페이스 패턴. 새 도메인 추가, Aggregate 설계, Command/Query 분리, Repository 구현, 도메인 이벤트 발행, ACL 구현 시 사용. DDD, aggregate, entity, value object, domain event, repository, use case, CQRS, command, query, ACL, anti-corruption layer, 외부 시스템 연동, 이벤트 변환 키워드에 트리거.
---

# Tactical Design — DDD 전술적 설계 패턴

Spring Boot + jOOQ 환경에서의 DDD 전술적 설계 가이드.
도메인별 패키징(Package by Domain) + Simplified CQRS + Hexagonal 의존성 방향을 결합한 실용적 패턴.

## 핵심 원칙

### 의존성 규칙 (The Dependency Rule)

의존성은 **안쪽으로만** 향한다. 바깥 레이어가 안쪽 레이어에 의존하며, 그 반대는 허용하지 않는다.

```
Infrastructure → Application → Domain
  (adapters)     (use cases)    (core)
```

**위반 징후:**
- Domain 클래스가 jOOQ, Spring 어노테이션을 import
- Controller가 Repository를 직접 호출 (Query 쪽 제외)
- Entity가 Application Service에 의존

**검증:** Domain 레이어의 코드를 테스트할 때 Spring Context나 DB 없이 순수 단위 테스트가 가능하면 경계가 올바른 것이다.

### CQRS — Command와 Query의 분리

상태를 변경하는 Command와 데이터를 조회하는 Query는 서로 다른 경로를 탄다.

```
[Command 흐름]
Api → UseCase(interface) → Service(POJO) → Repository(interface) → JOOQ Impl

[Query 흐름]  
QueryApi → jOOQ DAO (ctx()) → Response DTO
```

Query 쪽에서 Service 레이어를 거치지 않는 이유는, 조회는 도메인 로직이 필요 없고 DB에서 DTO로 직접 변환하는 것이 더 단순하고 성능적으로 유리하기 때문이다.

## 디렉토리 구조

도메인 우선 패키징을 사용한다. 각 도메인 내부에서 레이어를 나눈다.

```
domain/{context}/{subdomain}/
├── api/                          # Presentation Layer
│   ├── {Subdomain}Api.java       #   Command controller (POST, PUT, DELETE)
│   ├── {Subdomain}QueryApi.java  #   Query controller (GET)
│   ├── {Subdomain}EventConsumer.java  # Domain Event 수신 (선택)
│   └── dto/                      #   Request/Response DTO
│       ├── {Action}Request.java
│       └── {Find}Response.java
├── application/                  # Application Layer
│   ├── {Subdomain}Service.java   #   UseCase 구현 (POJO, jOOQ 의존 없음)
│   ├── command/                  #   Command DTO (Java Record)
│   │   └── {Action}Command.java
│   └── usecase/                  #   UseCase 인터페이스 (단일 메서드)
│       └── {Action}UseCase.java
├── domain/                       # Domain Layer (순수 Java, 외부 의존 없음)
│   ├── {Subdomain}.java          #   Aggregate Root (extends AggregateRoot)
│   ├── {Subdomain}Repository.java #  Repository 인터페이스
│   ├── {VO}.java                 #   Value Object
│   ├── vo/                       #   Value Object 디렉토리 (다수일 때)
│   └── event/                    #   Domain Event (Record, implements DomainEvent)
│       └── {PastTense}Event.java
└── infrastructure/               # Infrastructure Layer
    ├── JOOQ{Subdomain}Repository.java  # Repository 구현
    └── {External}Adapter.java          # 외부 시스템 어댑터 (선택)
```

## Quick Decision Trees

### "이 코드는 어디에 넣어야 하나?"

```
어디에 넣을까?
├─ 순수 비즈니스 로직, I/O 없음         → domain/
├─ 도메인 객체를 조율, 부수효과 있음     → application/ (Service)
├─ 외부 시스템과 통신                    → infrastructure/
├─ 상태 변경 API 엔드포인트             → api/ (*Api.java)
└─ 조회 API 엔드포인트                  → api/ (*QueryApi.java)
```

### "Entity인가 Value Object인가?"

```
├─ 고유 식별자가 있고 생명주기가 있다   → Entity
├─ 속성 값으로만 정의된다               → Value Object
├─ "같은 것인가?" (identity 비교)       → Entity
└─ "같은 값인가?" (structural 비교)     → Value Object
```

### "같은 Aggregate인가 분리해야 하나?"

```
├─ 한 트랜잭션 내에서 일관성 필요       → 같은 Aggregate
├─ 최종 일관성(eventual)으로 충분       → 별도 Aggregate
├─ ID로만 참조                          → 별도 Aggregate
└─ Aggregate 내 Entity가 10개 이상      → 분리 검토
```

**규칙:** 하나의 트랜잭션에 하나의 Aggregate. Cross-Aggregate 일관성은 Domain Event로 처리.

## DDD Building Blocks

| 패턴 | 목적 | 레이어 | 핵심 규칙 |
|------|------|--------|-----------|
| **AggregateRoot** | 일관성 경계 + 이벤트 수집 | Domain | `extends AggregateRoot`, 팩토리 메서드로 생성 |
| **Entity** | 식별자 + 행위 | Domain | ID 기반 동등성 (`equals`/`hashCode`) |
| **Value Object** | 불변 데이터 | Domain | 값 기반 동등성, setter 없음 |
| **Domain Event** | 변경 사실 기록 | Domain | `record ... implements DomainEvent`, 과거형 이름 |
| **Repository** | 영속화 추상화 | Domain(interface) | Aggregate당 1개, 테이블당 아님 |
| **UseCase** | 단일 유스케이스 인터페이스 | Application | 인터페이스 1개 = 메서드 1개 |
| **Command** | 명령 DTO | Application | Java Record, 불변 |
| **Service** | 유스케이스 구현 (오케스트레이션) | Application | POJO, jOOQ 의존 없음 |

## Anti-Patterns

| Anti-Pattern | 문제 | 해결 |
|-------------|------|------|
| **빈약한 도메인 모델** | Entity에 getter만 있고 로직은 Service에 | 행위를 Entity 안으로 이동 |
| **테이블당 Repository** | Aggregate 경계를 무시 | Aggregate당 1개 Repository |
| **인프라 누수** | Domain이 jOOQ/Spring을 import | Domain은 순수 Java만 |
| **거대 Aggregate** | 너무 많은 Entity, 느린 트랜잭션 | 작은 Aggregate로 분리 |
| **Port 우회** | Controller → Repository 직접 호출 | Command는 반드시 UseCase 경유 |
| **CRUD 사고** | 데이터 모델링이 아닌 행위 모델링 | 비즈니스 오퍼레이션 중심 설계 |

## 참조 문서

| 파일 | 목적 |
|------|------|
| [references/LAYERS.md](references/LAYERS.md) | 각 레이어별 상세 구현 패턴과 실제 코드 예시 |
| [references/CQRS.md](references/CQRS.md) | Command/Query 분리 상세 패턴, jOOQ DAO 활용법 |
| [references/DDD-TACTICAL.md](references/DDD-TACTICAL.md) | AggregateRoot, Entity, Value Object, Domain Event 상세 |

새 도메인 추가 시 → LAYERS.md를 먼저 읽고 각 레이어별 코드를 순서대로 구현.
CQRS 패턴 이해 필요 시 → CQRS.md 참조.
Aggregate 설계 고민 시 → DDD-TACTICAL.md 참조.