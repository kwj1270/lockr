새로운 기능 구현을 위한 TDD Plan 파일을 생성합니다.

기능 이름: $ARGUMENTS

## 진행 순서
1. 기능 요구사항을 사용자에게 확인합니다
2. 관련 도메인 컨텍스트를 읽어 기존 구조를 파악합니다
3. TDD Plan 파일을 `docs/plans/{기능이름}-plan.md` 경로에 생성합니다

## Plan 파일 템플릿
```markdown
# {기능 이름} 구현 계획

## 개요
{기능 설명}

## 테스트 목록

### Phase 1: Domain Layer

- [ ] {테스트 설명 1}
- [ ] {테스트 설명 2}

### Phase 2: Application Layer

- [ ] {테스트 설명 3}

### Phase 3: API Layer

- [ ] {테스트 설명 4}

---

## 완료 기준
- [ ] 표시: 미완료
- [x] 표시: 완료

"go" 명령 시 첫 번째 미완료 테스트부터 순서대로 진행
```

4. CLAUDE.md의 PLAN FILES 섹션에 새 plan 파일을 추가합니다