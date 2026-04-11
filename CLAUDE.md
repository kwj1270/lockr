# lockr-server — Backend CLAUDE.md

Spring Boot + jOOQ 기반 축구 동호회 관리 서버. DDD + Simplified CQRS 아키텍처.

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
Application (use cases) -> Domain (core, 순수 Java) <- Infrastructure (adapters) 
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

도메인별 컨텍스트는 각 도메인 디렉토리의 CLAUDE.md에서 관리 (`.claude/rules/routers/domain-*.md`가 라우터 역할):

| 도메인 | 컨텍스트 위치 | 특성 |
|--------|-------------|------|
| club | `domain/club/CLAUDE.md` | 가장 큰 BC, 다수 subdomain |
| auth | `domain/auth/CLAUDE.md` | 인증/인가, OIDC |
| users | `domain/users/CLAUDE.md` | 사용자 프로필, 탈퇴 이벤트 |
| notification | `domain/notification/CLAUDE.md` | 푸시 알림, FCM |
| shorts | `domain/shorts/CLAUDE.md` | 숏폼 콘텐츠 |
| home | `domain/home/CLAUDE.md` | Query-only, 도메인 레이어 없음 |

새 기능 구현 전 해당 도메인의 컨텍스트 문서를 먼저 확인할 것.

## 에이전트 & 스킬

### 에이전트 (`.claude/agents/`)

| 에이전트 | 트리거 키워드 | 역할 |
|----------|-------------|------|
| `domain-audit` | 도메인감사, DDD검사, 도메인규칙 | CQRS/DDD 아키텍처 준수 감사 |
| `event-tracer` | 이벤트추적, 도메인이벤트, 이벤트흐름 | Domain Event 발행/구독 추적 |
| `feature-planner` | (build-feature 내부) | 기능 구현 계획서 생성 (코드 작성 안 함) |
| `feature-executor` | (build-feature 내부) | 계획서 기반 기능 구현 |
| `migration-sync` | 마이그레이션동기화, DB동기화, JOOQ | Flyway + jOOQ 동기화 |
| `new-domain` | 새도메인, 도메인생성, 도메인추가 | 새 도메인 CQRS 레이어 설계 (코드 작성 안 함) |
| `test-runner` | 서버테스트, gradle test | 테스트 실행 및 결과 분석 |
| `test-scaffold` | 테스트스캐폴딩, 테스트갭 | 테스트 커버리지 갭 분석 및 우선순위 리포트 |

### 스킬 (`.claude/skills/`)

| 스킬 | 트리거 | 역할 |
|------|--------|------|
| `adr` | `/adr {제목}` | Architecture Decision Record 생성 |
| `research` | `/research {주제}` | 도메인/기술 리서치 |
| `tactical-design` | DDD, aggregate, entity, CQRS | DDD 전술적 설계 패턴 가이드 |
| `strategic-design` | bounded context, context map, subdomain | DDD 전략적 설계 패턴 가이드 |
| `build-feature` | `/build-feature {도메인} {기능}` | Plan → Implement → Audit 3-에이전트 파이프라인 |

### Hooks (자동 실행, `.claude/hooks/`)

| Hook | 감지 대상 |
|------|----------|
| `check-domain-purity` | domain/ 레이어에 jOOQ/Spring/Jakarta import 유입 |
| `suggest-adr` | 새 의존성/마이그레이션/Aggregate 추가 시 ADR 작성 제안 |
| `suggest-doc-regen` | domain/ 하위 Aggregate/Event/Enum 변경 시 문서 재생성 안내 |
| `check-feature-drift` | Api 파일 변경 시 대응 .feature 파일 동기화 알림, 새 도메인 feature 누락 감지 |
| `check-claudemd-drift` | 모든 도메인 AggregateRoot 변경 시 해당 도메인 CLAUDE.md 동기화 확인 |

### 스크립트 (수동 실행, `.claude/scripts/`)

| 스크립트 | 명령어 | 역할 |
|---------|--------|------|
| Domain Docs | `bash .claude/scripts/generate-domain-docs.sh` | Event Flow + Aggregate Overview + 용어사전 통합 생성 |
| 테스트 커버리지 | `bash .claude/scripts/check-domain-test-coverage.sh` | Aggregate 비즈니스 메서드 테스트 누락 리포트 |

### 하네스 디렉토리 구조

```
.claude/
├── agents/
│   ├── domain-audit.md
│   ├── event-tracer.md
│   ├── feature-planner.md          # build-feature Phase 1
│   ├── feature-executor.md         # build-feature Phase 2
│   ├── migration-sync.md
│   ├── new-domain.md
│   ├── test-runner.md
│   └── test-scaffold.md
├── skills/
│   ├── adr/SKILL.md
│   ├── build-feature/SKILL.md      # 오케스트레이터 (planner → executor → audit)
│   ├── research/SKILL.md
│   ├── tactical-design/
│   │   ├── SKILL.md
│   │   └── references/ (LAYERS, CQRS, DDD-TACTICAL)
│   └── strategic-design/
│       ├── SKILL.md
│       └── references/ (BOUNDED-CONTEXT, CONTEXT-MAPPING, EVENT-STORMING)
├── hooks/ (5개 — domain-purity, adr, doc-regen, feature-drift, claudemd-drift)
├── rules/
│   ├── routers/ (6개 — 도메인별 CLAUDE.md 라우터)
│   └── cross-cutting/ (5개 — test, tdd, db, api, cqrs)
└── scripts/ (generate-domain-docs, check-domain-test-coverage)
```

## docs/ 구조

```
docs/
├── adr/                          # ADR (수동, /adr 스킬)
│   ├── TEMPLATE.md
│   ├── README.md                 # 인덱스
│   └── 0001~0004.md              # 아키텍처 결정 기록
├── models/                       # 다이어그램 (자동 + Claude 스킬)
│   ├── event-flow.md             # 이벤트 발행/구독 (자동: generate-domain-docs.sh)
│   ├── class-diagrams/
│   │   └── _overview.md          # Aggregate 관계도 (자동: generate-domain-docs.sh)
│   ├── context-map.md            # BC간 관계도 (TODO: 반자동)
│   ├── state-diagrams/           # 상태 전이 (TODO: Claude 스킬로 생성)
│   └── sequence-diagrams/        # Command + Saga 흐름 (TODO: Claude 스킬로 생성)
└── glossary.md                   # 용어사전 (자동: generate-domain-docs.sh + 수동 보완)
```

비즈니스 규칙은 각 도메인 CLAUDE.md에서 직접 관리 (docs/invariants/ 폐기).

**자동 생성 갱신:** `bash .claude/scripts/generate-domain-docs.sh`
**TODO 항목:** context-map, state-diagrams, sequence-diagrams는 grep으로 추출 불가 → Claude 스킬로 생성 예정

## 하네스 변경 이력

| 날짜       | 변경 내용                                                                  | 대상                       | 사유                                                     |
|------------|---------------------------------------------------------------------------|----------------------------|----------------------------------------------------------|
| 2025-12    | 초기 에이전트 6개 + 스킬 3개 구성                                         | 전체                       | lockr-server DDD 자동화 기반                             |
| 2026-03    | spec, adr 스킬 추가                                                       | skills/                    | 기능 기획 → 구현 워크플로우 체계화                       |
| 2026-04-04 | tactical/strategic-design ACL 보강                                         | skills/                    | cufit ACL 분석 피드백 반영                                |
| 2026-04-04 | generate-event-flow.sh 버그 수정                                           | scripts/                   | awk dedup이 mermaid end 태그 제거하던 문제               |
| 2026-04-05 | Docs 하네스 구축 (generate-domain-docs.sh, ADR 0002-0004, hooks 추가)      | scripts/, docs/, hooks/    | 반자동 문서 생성 시스템                                  |
| 2026-04-06 | 하네스 감사 — 파일/CLAUDE.md 동기화 확인, 변경 이력 추가                   | CLAUDE.md                  | harness 플러그인 도입, 진화 추적                         |
| 2026-04-10 | Rules(라우터) + 도메인 CLAUDE.md 2계층 하네스 구축                          | rules/, domain/*/CLAUDE.md | BC별 컨텍스트 분리, 순수 라우터, 비즈니스 규칙 직접 기술 |
| 2026-04-11 | Hook 범용화 + 중복 스크립트 제거 + settings.local.json 정리                 | hooks/, scripts/, settings | club-only drift→전도메인, event-flow.sh 중복 제거, 임시 규칙 정리 |
| 2026-04-11 | build-feature 3-에이전트 파이프라인으로 리팩터링                             | agents/, skills/build-feature | Plan/Implement/Audit 역할 분리 (feature-planner, feature-executor 추가) |

## 개발 워크플로우

```
1. /adr {설계 결정} → Aggregate 경계 등 아키텍처 결정 기록
2. TDD Plan Phase별 구현 (tactical-design 스킬 자동 참조)
3. 구현 후 반드시 리뷰:
   └─ @code-reviewer → 코드 품질 리뷰
   └─ @domain-audit {도메인명} → DDD 아키텍처 준수 검증
4. bash .claude/scripts/check-domain-test-coverage.sh → 테스트 누락 확인
5. bash .claude/scripts/generate-domain-docs.sh → 도메인 문서 갱신
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
