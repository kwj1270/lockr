# lockr-server — Backend CLAUDE.md

Spring Boot + jOOQ 기반 스포츠 동호회 관리 서버. DDD + Simplified CQRS 아키텍처.

## Build & Test

```bash
# 빌드 (Java 24 필요)
./gradlew build -x test

# 테스트
./gradlew test
./gradlew test --tests "com.official.lockr.domain.club.schedule.*"  # 특정 도메인만

# jOOQ 코드 생성 (Docker MySQL 필요)
./gradlew generateJooqClasses
```

## Infrastructure

```bash
docker compose up -d mysql redis    # MySQL 8 (3306, root/1234, db:lockr) + Redis (6379)
```

Spring Boot가 앱 실행 시 Docker Compose를 자동 시작 (`spring.docker.compose.enabled=true`).

## 아키텍처 핵심 규칙

### 의존성 방향

```
Domain (core, 순수 Java)  ← 의존 없음
  ↑
Application (use cases)    ← Domain만 의존
  ↑
Infrastructure (adapters)  ← Application, Domain 의존
```

domain/ 패키지에 jOOQ, Spring, Jakarta import 금지 — Hook이 자동 감지.

### CQRS

```
[Command] Api → UseCase(interface) → Service → Repository(interface) → JOOQ Impl
[Query]   QueryApi → Configuration DI → new XxxDao(configuration) → dao.ctx() 직접 조회
```

Query에는 Service 레이어 없음. jOOQ DAO로 직접 조회.

### 패키지 구조

```
domain/{context}/{subdomain}/
├── api/          → *Api.java (Command), *QueryApi.java (Query), dto/
├── application/  → *Service.java, usecase/, command/
├── domain/       → AggregateRoot, Repository interface, VO, event/
└── infrastructure/ → JOOQ*Repository.java
```

### 핵심 패턴

- **AggregateRoot**: `extends AggregateRoot`, `init()` 팩토리 + `addEvent()`, `equals`/`hashCode` by ID
- **Domain Event**: `record ... implements DomainEvent`, 과거형 이름
- **UseCase**: 인터페이스 1개 = 메서드 1개
- **Command**: Java Record, 불변
- **ID**: ULID (`UlidUtils.generateUlid()`)
- **Event 발행**: Repository.save() 내에서 `aggregate.publish(domainEventPublisher)`
- **Request DTO**: `toCommand()` 메서드로 Command 변환

## DOMAIN CONTEXT

도메인 구조 파악은 코드를 직접 탐색하여 수행한다:

- `src/main/java/com/official/lockr/domain/auth/` - 인증 도메인 (Admin, OIDC, SignIn)
- `src/main/java/com/official/lockr/domain/users/` - 사용자 도메인
- `src/main/java/com/official/lockr/domain/club/` - 클럽 도메인 (Club, Schedule, Chat, Feed, Recruitment, Sport, Stats)
- `src/main/java/com/official/lockr/domain/notification/` - 알림 도메인
- `src/main/java/com/official/lockr/domain/home/` - 홈 도메인
- `src/main/java/com/official/lockr/domain/shorts/` - 숏폼 도메인

새 기능 구현 전 관련 도메인의 Aggregate Root, Event, Repository를 먼저 읽어 비즈니스 규칙과 도메인 관계를 파악할 것.

## 에이전트 & 스킬

### 에이전트 (`.claude/agents/`)

| 에이전트 | 트리거 키워드 | 역할 |
|----------|-------------|------|
| `domain-audit` | 도메인감사, DDD검사, 도메인규칙 | CQRS/DDD 아키텍처 준수 감사 |
| `event-tracer` | 이벤트추적, 도메인이벤트, 이벤트흐름 | Domain Event 발행/구독 추적 |
| `migration-sync` | 마이그레이션동기화, DB동기화, JOOQ | Flyway + jOOQ 동기화 |
| `new-domain` | 새도메인, 도메인생성, 도메인추가 | 새 도메인 스캐폴딩 |
| `test-runner` | 서버테스트, gradle test | 테스트 실행 및 결과 분석 |
| `test-scaffold` | 테스트스캐폴딩, 테스트생성 | 테스트 코드 자동 생성 |

### 스킬 (`.claude/skills/`)

| 스킬 | 트리거 | 역할 |
|------|--------|------|
| `spec` | `/spec {기능명}` | 서버 PRD + TDD Plan 생성 |
| `adr` | `/adr {제목}` | Architecture Decision Record 생성 |
| `research` | `/research {주제}` | 도메인/기술 리서치 |
| `tactical-design` | DDD, aggregate, entity, CQRS | DDD 전술적 설계 패턴 가이드 |
| `strategic-design` | bounded context, context map, subdomain | DDD 전략적 설계 패턴 가이드 |

### Hooks (자동 실행, `.claude/hooks/`)

| Hook | 감지 대상 |
|------|----------|
| `check-domain-purity` | domain/ 레이어에 jOOQ/Spring/Jakarta import 유입 |
| `suggest-adr` | 새 의존성/마이그레이션/Aggregate 추가 시 ADR 작성 제안 |

### 스크립트 (수동 실행, `.claude/scripts/`)

| 스크립트 | 명령어 | 역할 |
|---------|--------|------|
| Event Flow | `bash .claude/scripts/generate-event-flow.sh` | 이벤트 발행/구독 Mermaid 다이어그램 생성 |
| 테스트 커버리지 | `bash .claude/scripts/check-domain-test-coverage.sh` | Aggregate 비즈니스 메서드 테스트 누락 리포트 |

## 개발 워크플로우

```
1. /spec {기능명} → PRD + TDD Plan 생성
2. /adr {설계 결정} → Aggregate 경계 등 아키텍처 결정 기록
3. TDD Plan Phase별 구현 (tactical-design 스킬 자동 참조)
4. 구현 후 반드시 리뷰:
   └─ @code-reviewer → 코드 품질 리뷰
   └─ @domain-audit {도메인명} → DDD 아키텍처 준수 검증
5. bash .claude/scripts/check-domain-test-coverage.sh → 테스트 누락 확인
6. bash .claude/scripts/generate-event-flow.sh → 이벤트 다이어그램 갱신
```

## Seed 데이터 (로컬 개발용)

- 위치: `infra/mysql/seed/seed_data.sql`
- 관리자: admin / test1234
- 사용자: 80명 (각 클럽당 20명)
- 클럽 4개:
  - FC 강남 (강남구) - 회장1, 매니저2, 코치1, 일반16
  - 마포 FC (마포구) - 회장1, 매니저1, 코치2, 일반16
  - 송파 유나이티드 (송파구) - 회장1, 매니저1, 코치1, 일반17
  - 영등포 FC (영등포구) - 회장1, 매니저2, 코치1, 일반16
