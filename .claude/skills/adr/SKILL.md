---
name: adr
description: Architecture Decision Record 생성 및 관리. 새 ADR 작성, 기존 ADR 조회, ADR 인덱스 갱신. /adr, ADR, 아키텍처 결정, 기술 선택, 설계 결정 키워드에 트리거.
---

# ADR — Architecture Decision Record

아키텍처 결정을 기록하고 추적하는 도구.

## 언제 ADR을 작성하는가

- 새 기술/라이브러리 도입 (예: Redis, Firebase)
- 아키텍처 패턴 채택 (예: CQRS, Event Sourcing)
- Aggregate 경계 결정 (예: Schedule을 Club에서 분리)
- 외부 시스템 연동 방식 결정 (예: OIDC Provider 선택)
- 기존 결정을 뒤집을 때 (상태를 "대체됨"으로 변경)

간단한 기능 추가나 버그 수정은 ADR 대상이 아니다.

## 새 ADR 생성

사용자가 `/adr {제목}` 또는 "ADR 작성해줘"라고 하면:

1. `docs/adr/` 디렉토리에서 가장 큰 번호를 찾는다
2. 다음 번호로 `docs/adr/{NNNN}-{kebab-case-title}.md` 파일을 생성한다
3. `docs/adr/TEMPLATE.md`의 형식을 따른다
4. 사용자와 대화하며 컨텍스트, 선택지, 결정을 채운다
5. 완료 후 `docs/adr/README.md` 인덱스를 갱신한다

### 번호 부여 규칙

```bash
# 다음 번호 산출
NEXT=$(ls docs/adr/ | grep -oE '^[0-9]+' | sort -n | tail -1)
NEXT=$((NEXT + 1))
# 4자리 패딩: 0001, 0002, ...
```

### 파일명 규칙

```
docs/adr/0001-cqrs-pattern-adoption.md
docs/adr/0002-jooq-over-jpa.md
docs/adr/0003-ulid-for-id-generation.md
```

## ADR 작성 가이드

### 컨텍스트 (Context)

상황을 객관적으로 기술한다. 문제가 무엇인지, 왜 결정이 필요한지.

```markdown
## 컨텍스트

데이터 접근 계층에서 ORM을 선택해야 한다.
도메인 모델이 복잡하고 jOOQ의 타입 세이프 쿼리가 필요하며,
QueryApi에서 직접 SQL을 작성하는 CQRS 패턴을 사용할 예정이다.
```

### 선택지 (Options)

최소 2개 이상. 각각 장단점을 명시한다. "검토했지만 버린 것"도 기록해야 나중에 같은 논의를 반복하지 않는다.

### 결정 (Decision)

한 문장으로 명확하게. "X를 채택한다."

### 근거 (Rationale)

"왜"를 설명한다. 이것이 ADR의 핵심 가치 — 미래의 자신이 "왜 이렇게 했지?"라고 물을 때 답이 된다.

### 결과 (Consequences)

긍정적 결과와 부정적 결과 모두. 트레이드오프를 솔직하게 기록한다.

## ADR 인덱스 갱신

ADR 생성/수정 후 `docs/adr/README.md`를 갱신한다:

```markdown
# Architecture Decision Records

| # | 제목 | 상태 | 날짜 | 도메인 |
|---|------|------|------|--------|
| [0001](0001-cqrs-pattern.md) | CQRS 패턴 채택 | 승인됨 | 2025-03-15 | 전체 |
| [0002](0002-jooq-over-jpa.md) | JPA 대신 jOOQ 선택 | 승인됨 | 2025-03-15 | 전체 |
```

## 기존 결정 변경 시

기존 ADR을 수정하지 않는다. 대신:
1. 새 ADR을 작성한다
2. 기존 ADR의 상태를 "대체됨(by ADR-XXXX)"으로 변경한다
3. 새 ADR의 컨텍스트에 "ADR-YYYY를 대체한다"고 명시한다
