# ADR-0003: Entity ID로 ULID 선택

- **상태**: 승인됨
- **날짜**: 2026-04-04
- **관련 도메인**: 전체 (도메인 레이어 ID 생성)

## 컨텍스트

Lockr 서버의 모든 Aggregate와 Entity는 애플리케이션 레이어에서 ID를 생성한다. DB auto-increment에 의존하지 않는 구조이므로, 애플리케이션에서 고유하고 안전한 ID를 직접 생성해야 한다.

현재 코드베이스에서 확인되는 패턴:
- `global/util/UlidUtils.java`: `com.github.f4b6a3:ulid-creator:5.2.4` 라이브러리 사용
- `Club.init()`, `Member.init()`, `FeePolicy.init()`, `FeeRecord.init()`, `Attendance.init()` 등 모든 Aggregate/Entity 팩토리 메서드에서 `UlidUtils.generateUlid()` 호출
- `build.gradle`: `implementation 'com.github.f4b6a3:ulid-creator:5.2.4'` 명시
- 생성된 ID는 `String` 타입으로 저장 (`SCHEDULES.ID`, `CLUBS.ID` 등 모두 문자열 컬럼)

## 고려한 선택지

### 선택지 1: Auto-increment (DB 생성 Long)

DB가 순차적으로 증가하는 Long 값을 부여.

- 장점: 구현 단순, 인덱스 성능 최적 (B-tree에 순차 삽입)
- 단점: **ID를 사전에 알 수 없음** — 도메인 이벤트에 ID를 포함시키려면 DB 저장 후 ID를 읽어와야 함
- 단점: **분산 환경 불가** — 여러 노드에서 동일한 채번 불가능
- 단점: **예측 가능** — 순차 증가 ID는 크롤링/열거 공격에 취약

### 선택지 2: UUID v4

표준 128비트 랜덤 식별자.

- 장점: 분산 환경에서 충돌 없이 생성 가능, 예측 불가
- 장점: 언어/플랫폼 표준 지원
- 단점: **정렬 불가** — 완전 랜덤이라 시간순 정렬을 위한 별도 컬럼 필요
- 단점: **DB 인덱스 단편화** — 랜덤 삽입으로 B-tree 페이지 분할 빈번
- 단점: 36자 문자열 (하이픈 포함)로 저장 공간 낭비

### 선택지 3: ULID (Universally Unique Lexicographically Sortable Identifier)

128비트 식별자 = 48비트 타임스탬프 + 80비트 랜덤. Crockford Base32 인코딩으로 26자 문자열.

- 장점: **시간순 정렬 가능** — 타임스탬프 prefix로 생성 순서대로 정렬
- 장점: **DB 인덱스 성능** — 단조 증가하는 prefix 덕분에 B-tree 순차 삽입에 가까운 성능
- 장점: **26자** — UUID(36자)보다 짧아 저장/전송 효율적
- 장점: **분산 환경 안전** — 80비트 랜덤으로 충돌 확률 극히 낮음
- 장점: **애플리케이션에서 사전 생성 가능** — DB 저장 전에 ID를 알 수 있어 도메인 이벤트에 즉시 포함 가능
- 단점: 표준 라이브러리가 없어 외부 의존성 필요 (`ulid-creator`)
- 단점: UUID에 비해 생태계 지원이 제한적

## 결정

**ULID를 채택한다.** `com.github.f4b6a3:ulid-creator` 라이브러리를 통해 `UlidUtils.generateUlid()`로 일관되게 생성한다.

## 근거

Lockr의 도메인 설계에서 ID 사전 생성이 핵심 요구사항이다. `Club.init()` 등 팩토리 메서드가 DB 저장 전에 ID를 포함한 완전한 도메인 객체를 생성하고, `addEvent()`로 해당 ID를 포함한 도메인 이벤트를 등록한다. Auto-increment는 이 패턴과 근본적으로 불일치한다.

UUID v4 대비 ULID의 선택 이유는 두 가지다:
1. **정렬 가능성** — jOOQ Query 경로에서 `CREATED_AT` 없이 ID 기준 정렬도 시간순으로 동작
2. **인덱스 효율** — PK 인덱스 단편화를 줄여 대량 데이터에서 삽입 성능 유지

`UlidUtils`라는 단일 진입점으로 전 프로젝트에서 일관된 생성 방식을 강제한다.

## 결과

- 긍정: 팩토리 메서드에서 ID를 포함한 완전한 도메인 객체 생성 가능 → 도메인 이벤트에 즉시 ID 포함
- 긍정: UUID 대비 짧은 문자열(26자)로 URL, 로그, 응답 페이로드 경량화
- 긍정: 시간순 정렬이 ID 정렬과 일치하여 커서 기반 페이지네이션 구현 단순화
- 부정: `ulid-creator` 외부 라이브러리 의존성 추가
- 주의: ULID는 밀리초 단위 타임스탬프를 사용하므로 같은 밀리초 내 생성된 ULID는 랜덤 순서. 엄밀한 생성 순서가 필요한 경우 `createdAt` 컬럼 기준 정렬 사용
