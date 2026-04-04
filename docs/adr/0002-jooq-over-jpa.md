# ADR-0002: JPA 대신 jOOQ 선택

- **상태**: 승인됨
- **날짜**: 2026-04-04
- **관련 도메인**: 전체 (infrastructure 레이어)

## 컨텍스트

Lockr 서버는 Simplified CQRS 패턴을 채택하고 있다. Command 경로에서는 도메인 모델을 통한 저장이 필요하고, Query 경로에서는 여러 테이블을 JOIN하거나 집계하는 복잡한 조회가 빈번하게 발생한다. 데이터 접근 기술을 선택해야 했다.

현재 코드베이스에서 확인되는 패턴:
- `JOOQScheduleRepository`, `JOOQClubRepository` 등 27개 Repository가 모두 jOOQ로 구현됨
- `ScheduleQueryApi`, `ClubQueryApi` 등 QueryApi에서 `dao.ctx()`로 DSLContext에 직접 접근
- `build.gradle`에 `spring-boot-starter-jooq` + `jooq:3.19.31` 명시, JPA/Hibernate 의존성 없음
- `dev.monosoul.jooq-docker` 플러그인으로 Docker 기반 코드 생성 파이프라인 구축
- `JPrefixGeneratorStrategy`로 생성된 타입 세이프 메타데이터 클래스 사용 (예: `SCHEDULES.ID`, `CLUBS.NAME`)

## 고려한 선택지

### 선택지 1: JPA (Hibernate)

Spring 생태계에서 가장 널리 쓰이는 ORM.

- 장점: Spring Data Repository로 기본 CRUD 자동화, 학습 자료 풍부
- 장점: 엔티티 매핑 선언적으로 표현 가능
- 단점: **N+1 문제** — 연관 엔티티를 Lazy Loading으로 가져올 때 쿼리 폭발 위험
- 단점: **복잡한 집계 쿼리 표현 어려움** — JPQL/Criteria API로 GROUP BY, 서브쿼리 등을 표현하면 가독성 저하
- 단점: **Simplified CQRS Query 경로와 불일치** — 도메인 모델 없이 DTO 직접 조회가 필요한 Query 경로에서 JPA 영속성 컨텍스트가 오히려 오버헤드
- 단점: 실행 SQL을 컴파일 타임에 검증할 수 없어 런타임 오류 위험

### 선택지 2: jOOQ

타입 세이프 SQL 빌더 + 코드 생성 기반의 데이터 접근 라이브러리.

- 장점: **타입 세이프 쿼리 빌더** — `SCHEDULES.ID`, `CLUBS.NAME` 등 생성된 메타데이터로 컴파일 타임 오류 감지
- 장점: **Query 경로 직접 조회** — `dao.ctx()`로 DSLContext에 접근해 복잡한 JOIN/집계를 SQL에 가깝게 표현
- 장점: **스키마 동기화 보장** — Docker 기반 코드 생성이 Flyway 마이그레이션을 적용한 실제 DB에서 클래스를 생성하므로 코드와 스키마가 항상 일치
- 장점: **N+1 없음** — 개발자가 쿼리를 직접 제어하므로 암묵적 추가 쿼리 없음
- 단점: 코드 생성 파이프라인 유지 필요 (`generateJooqClasses` 실행)
- 단점: JPA보다 선언적 매핑이 적어 Repository 구현 코드량이 더 많음

## 결정

**jOOQ를 채택한다.** JPA는 사용하지 않는다.

## 근거

Simplified CQRS 구조에서 Query 경로와 Command 경로 모두에 jOOQ가 적합하다.

| 기준 | JPA | jOOQ |
|------|-----|------|
| Query 경로 DTO 직접 조회 | JPQL + Projections, 번거로움 | `fetchInto(Dto.class)`, 간결 |
| 복잡한 JOIN/집계 | Criteria API 장황 | SQL에 가까운 표현 |
| 컴파일 타임 검증 | 불가 (JPQL 문자열) | 가능 (생성된 메타데이터) |
| 스키마 동기화 | 마이그레이션과 엔티티 불일치 가능 | 코드 생성으로 항상 일치 |
| N+1 위험 | 존재 | 없음 (쿼리 직접 제어) |

`ClubQueryApi`에서 `clubsDao.ctx().select(...).from(CLUBS).join(MEMBERS)...` 형태로 여러 테이블을 JOIN하고 집계하는 코드가 jOOQ 선택의 실질적 근거다. JPA로 동일한 조회를 표현하면 JPQL이 장황해지거나 네이티브 쿼리로 빠져나가야 한다.

## 결과

- 긍정: Query 경로에서 복잡한 조회를 SQL에 가깝게 간결하게 표현 가능
- 긍정: 스키마 변경 시 `generateJooqClasses` 실행만으로 코드와 DB가 동기화됨
- 긍정: 컴파일 타임에 컬럼명·타입 오류 감지
- 부정: 스키마 변경 후 코드 생성을 반드시 실행해야 빌드 성공 (`./gradlew generateJooqClasses`)
- 부정: Repository 구현 코드량이 JPA보다 많음 (Entity ↔ Domain POJO 변환 직접 작성)
- 주의: Command 경로에서도 jOOQ upsert 패턴(`insertInto(...).onDuplicateKeyUpdate()`)을 일관되게 사용
