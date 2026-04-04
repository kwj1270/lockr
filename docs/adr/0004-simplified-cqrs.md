# ADR-0004: Simplified CQRS 패턴 채택

- **상태**: 승인됨
- **날짜**: 2026-04-04
- **관련 도메인**: 전체 (아키텍처 패턴)

## 컨텍스트

스포츠 동호회 관리 서비스인 Lockr는 다양한 조회 요구사항을 가진다. 일정 목록에 클럽명·출석 현황을 함께 보여주거나, 클럽 멤버 목록에 스쿼드 포지션·연락처를 함께 조회하는 등 여러 테이블을 JOIN하는 Read 요청이 많다. 반면 상태를 변경하는 Write 요청은 도메인 불변식을 반드시 지켜야 한다.

현재 코드베이스에서 확인되는 패턴:
- Command 경로: `ClubApi` → `FoundClubUseCase` → `ClubService` → `ClubRepository` → `JOOQClubRepository`
- Query 경로: `ClubQueryApi(Configuration)` → `clubsDao.ctx().select(...).fetchInto(Dto.class)`
- `*Api.java` (POST/PUT/DELETE) 와 `*QueryApi.java` (GET) 가 15개 도메인 전체에서 분리됨
- `ScheduleQueryApi`에서 5단계 분리 쿼리로 집계 조회 수행 — Service 레이어 없음
- Command 경로 Service는 `ClubRepository`(도메인 인터페이스)만 의존, jOOQ 의존성 없음

## 고려한 선택지

### 선택지 1: 단일 Service 패턴

Command와 Query를 동일한 Service에서 처리.

- 장점: 구조 단순, 파일 수 적음
- 단점: **Service 비대화** — 복잡한 조회 로직과 도메인 불변식 검증 로직이 혼재
- 단점: **도메인 모델 오용** — 단순 목록 조회에서도 도메인 Aggregate를 거쳐야 해 불필요한 변환 비용
- 단점: **테스트 복잡도 증가** — 조회와 변경을 동시에 검증해야 함

### 선택지 2: Full CQRS (별도 Read DB)

Command용 Write DB와 Query용 Read Replica를 분리하고 이벤트 기반으로 동기화.

- 장점: Read/Write 부하 독립적 확장 가능
- 장점: Read 모델을 조회 요건에 맞게 최적화 가능
- 단점: **인프라 복잡성** — Read Replica, 이벤트 브로커, 동기화 지연 처리 필요
- 단점: **현재 규모에서 과도한 투자** — 동호회 관리 앱의 트래픽 규모에서 별도 DB 분리는 불필요
- 단점: 최종 일관성(Eventual Consistency) 처리 로직 추가 필요

### 선택지 3: Simplified CQRS (같은 DB, 다른 코드 경로)

Command와 Query가 동일한 DB를 사용하되, 코드 경로를 분리. Query는 Service 없이 QueryApi에서 jOOQ DAO로 직접 조회.

- 장점: **Query 경로 단순화** — 도메인 모델 로딩 없이 필요한 컬럼만 조회하여 성능 우수
- 장점: **인프라 복잡성 없음** — 같은 DB를 사용하므로 동기화 문제 없음
- 장점: **필요 시 Full CQRS로 진화 가능** — QueryApi를 Read Replica로 전환해도 Command 경로 변경 불필요
- 장점: **도메인 순수성 유지** — Command 경로의 Service/Domain 레이어에 jOOQ 의존성 없음
- 단점: `*Api.java`와 `*QueryApi.java` 파일이 도메인마다 분리되어 파일 수 증가
- 단점: Query 로직이 QueryApi에 직접 작성되어 재사용이 어려울 수 있음

## 결정

**Simplified CQRS를 채택한다.** Command와 Query를 코드 경로 수준에서 분리하되 동일한 DB를 사용한다.

## 근거

Lockr의 Read 요구사항은 도메인 모델보다 쿼리 최적화가 중요하다. `ScheduleQueryApi`에서 확인되듯 일정 조회 시 멤버 클럽 목록, 일정 ID 목록, 출석 상태, 출석 집계, 일정+클럽명 JOIN을 5단계로 나눠 수행한다. 이를 Service → Repository 경로로 강제하면 Aggregate 전체를 로드하고 다시 DTO로 변환하는 불필요한 과정이 생긴다.

Command 경로에서는 `ClubService`가 `ClubRepository` 인터페이스(도메인 레이어)만 의존하고 jOOQ를 전혀 모른다. 이 분리 덕분에 도메인 레이어에서 순수 Java 비즈니스 규칙 테스트가 가능하다.

```
[Command]
Api → UseCase(interface) → Service(순수 Java) → Repository(interface) → JOOQRepository(@Transactional)

[Query]
QueryApi(Configuration DI) → dao.ctx() → fetchInto(ResponseDto.class)
```

Full CQRS는 현재 규모에서 불필요한 인프라 투자다. Simplified CQRS는 코드 분리만으로 동일한 이점을 얻을 수 있다.

## 결과

- 긍정: Query 경로에서 필요한 컬럼만 조회하여 불필요한 도메인 Aggregate 로딩 없음
- 긍정: Command 경로의 Service/Domain이 jOOQ에 독립적 → 도메인 단위 테스트 용이
- 긍정: `*QueryApi`에서 jOOQ JOIN/집계를 자유롭게 구사 가능
- 긍정: 향후 트래픽 증가 시 QueryApi의 `Configuration`만 Read Replica를 가리키도록 변경하면 Full CQRS로 전환 가능
- 부정: 도메인마다 `*Api.java` + `*QueryApi.java` 두 파일이 생겨 파일 수 증가
- 부정: Query 로직이 QueryApi 클래스에 집중되어 복잡한 조회는 클래스가 길어질 수 있음
- 주의: Query 경로에서 이벤트 발행이 필요한 경우 Command로 분류하거나 별도 처리 필요 (Query는 부수 효과 없음)
