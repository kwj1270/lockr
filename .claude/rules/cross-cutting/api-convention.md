# API Convention

## HTTP Method

GET과 POST만 사용한다.

| 용도 | Method | 비고 |
|------|--------|------|
| 조회 | GET | Query (상태 변경 없음) |
| 생성/수정/삭제/행위 | POST | Command (상태 변경) |

PUT, PATCH, DELETE 사용 금지.

## URL 경로 규칙

### 네이밍
- **kebab-case** 사용 (`fee-records` O, `feeRecords` X)
- 리소스는 **복수형 명사** (`/schedules`, `/feeds`, `/members`)
- 합성어 리소스는 kebab-case 복수형 (`/fee-policies`, `/fee-records`)

### 계층 구조
상위 리소스 → 하위 리소스 순서로 중첩한다.

```
/api/v1/{상위리소스}/{상위ID}/{하위리소스}/{하위ID}
```

예시:
```
/api/v1/clubs/{clubId}/fee-policies
/api/v1/clubs/{clubId}/fee-records
/api/v1/clubs/{clubId}/schedules/{scheduleId}
/api/v1/clubs/{clubId}/members/{memberId}
```

### 행위(동사) 표현
리소스 CRUD로 표현할 수 없는 행위는 경로 마지막에 동사를 붙인다.

```
POST /api/v1/clubs/{clubId}/fee-records/notify-unpaid
POST /api/v1/clubs/{clubId}/members/{memberId}/remove
POST /api/v1/clubs/{clubId}/president/delegate
```

### 삭제
삭제 행위는 의미에 맞는 동사를 사용한다.

```
POST /api/v1/clubs/{clubId}/members/{memberId}/remove    (멤버 제거)
POST /api/v1/clubs/{clubId}/schedules/{scheduleId}/cancel (일정 취소)
POST /api/v1/clubs/{clubId}/feeds/{feedId}/delete         (피드 삭제)
```

## Controller 분리 (CQRS)

- `*Api.java`: POST 처리 (Command)
- `*QueryApi.java`: GET 처리 (Query)

두 컨트롤러는 동일한 `@RequestMapping` base path를 공유한다.

## Base Path 패턴

```java
// 클럽 하위 subdomain — base path에 clubId까지만
@RequestMapping("/api/v1/clubs/{clubId}")

// 각 메서드에서 리소스 경로 지정
@PostMapping("/fee-policies")
@GetMapping("/fee-records")
```

subdomain별 `/fee`, `/schedule` 같은 중간 prefix를 두지 않는다.
리소스명 자체가 소속을 표현한다 (`fee-policies`, `fee-records`).
