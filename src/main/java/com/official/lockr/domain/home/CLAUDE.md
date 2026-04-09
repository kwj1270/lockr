# Home Bounded Context

홈 화면 집계 도메인. **Query-only BC** — Aggregate Root, Domain Event, Domain Layer 없음.

## API

| API | 역할 | 패턴 |
|-----|------|------|
| HomeCardApi | 핀 클럽 카드 표시 (멤버 수, 다음 일정) | Query + pinClubs() Write |
| HomeScheduleApi | 사용자 참석 일정 집계 (시간 범위) | Query only |
| HomeNoticeApi | 소속 클럽 최신 공지 목록 | Query only |

## Gotchas

- `pinClubs()`만 유일한 Write 작업 — jOOQ 직접 사용 (도메인 레이어 우회)
- Configuration DI → jOOQ DAO → ctx() 직접 조회 (CQRS Query 패턴)
- Club, Schedule, Feed 데이터를 집계하지만 해당 도메인에 의존하지 않음 (DB 레벨 조회)
