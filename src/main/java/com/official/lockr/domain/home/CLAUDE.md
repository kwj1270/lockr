# Home Bounded Context

홈 화면 집계 도메인. 대부분 **Query-only** — banner subdomain만 예외적으로 Command/Domain Layer 보유.

## Subdomain Map

| Subdomain | Aggregate Root | 핵심 역할 |
|-----------|---------------|----------|
| card | (없음) | 핀 클럽 카드 표시 (Query + pinClubs Write) |
| schedule | (없음) | 사용자 참석 일정 집계 (Query only) |
| notice | (없음) | 소속 클럽 최신 공지 목록 (Query only) |
| banner | Banner | 홈 배너 CRUD (ADMIN 전용 Command + 사용자 Query) |

## API

| API | 역할 | 패턴 |
|-----|------|------|
| HomeCardApi | 핀 클럽 카드 표시 (멤버 수, 다음 일정) | Query + pinClubs() Write |
| HomeScheduleApi | 사용자 참석 일정 집계 (시간 범위) | Query only |
| HomeNoticeApi | 소속 클럽 최신 공지 목록 | Query only |
| BannerApi | 배너 등록/수정/삭제/활성토글/순서변경 | Command (ADMIN) |
| BannerQueryApi | 활성 배너 목록 조회 | Query only |

## Banner Subdomain

### Aggregate: Banner

| 필드 | 타입 | 설명 |
|------|------|------|
| id | String (ULID) | PK |
| title | String | 메인 헤드라인 |
| subtitle | String | 보조 텍스트 |
| placement | BannerPlacement | 배너 위치 (HOME_TOP) |
| imageUrl | String? | 배너 이미지 URL |
| actionUrl | String? | 외부 링크 |
| actionRoute | String? | 내부 라우트 |
| bgColor | String? | 배경색 hex |
| textColor | String? | 글자색 hex |
| iconType | String? | 아이콘 타입 |
| displayOrder | int | 노출 순서 (ASC) |
| active | boolean | 활성 여부 |

### Enum: BannerPlacement

| 값 | 설명 |
|-----|------|
| HOME_TOP | 홈 화면 최상단 캐러셀 |

### 비즈니스 규칙

| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| activate(count) | 동일 placement 활성 배너 최대 5개 | IllegalStateException |
| activate(count) | 이미 active면 early return (멱등) | — |
| deactivate() | 이미 inactive면 early return (멱등) | — |
| delete | 물리 삭제 | — |

### Domain Events

v1에서 Domain Event 없음. 향후 노출/클릭 추적 이벤트 추가 예정.

### Cross-Domain Dependencies

| 외부 도메인 | 방향 | 참조 | 설명 |
|------------|------|------|------|
| auth/admin | banner → auth (API 레벨) | AdminDao 직접 조회 | ADMIN 권한 체크. 향후 AdminInterceptor로 전환 가능 |

## Gotchas

- `pinClubs()`만 유일한 Write 작업 — jOOQ 직접 사용 (도메인 레이어 우회)
- Configuration DI → jOOQ DAO → ctx() 직접 조회 (CQRS Query 패턴)
- Club, Schedule, Feed 데이터를 집계하지만 해당 도메인에 의존하지 않음 (DB 레벨 조회)
- BannerApi에서 admin 테이블을 jOOQ로 직접 조회하여 ADMIN 권한 체크 (기존 AdminInterceptor 부재로 인한 현실적 트레이드오프)
- Banner는 v1에서 Domain Event 없음 — JOOQBannerRepository에 DomainEventPublisher 주입 안 함
