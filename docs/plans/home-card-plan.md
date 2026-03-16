# Home Card 기능 구현 계획

## 관련 파일
- Domain: `src/main/java/com/official/lockr/domain/home/card/domain/HomeCard.java`
- Repository: `src/main/java/com/official/lockr/domain/home/card/domain/HomeCardRepository.java`
- Infrastructure: `src/main/java/com/official/lockr/domain/home/card/infrastructure/JooqHomeCardRepository.java`
- API: `src/main/java/com/official/lockr/domain/home/card/api/HomeCardApi.java`
- Test: `src/test/java/com/official/lockr/domain/home/`
- Context: `docs/domains/home-context.md`

---

## 개요
홈 화면에 사용자가 핀(고정)한 클럽을 카드 형태로 표시하는 기능

## API 명세

### GET /api/v1/home/cards
핀된 클럽 카드 목록 조회

**Response:**
```json
{
  "cards": [
    {
      "clubId": "string",
      "name": "string",
      "emblemUrl": "string | null",
      "backgroundColor": "string",
      "sport": "string",
      "userRole": "string",
      "memberCount": number,
      "location": "string",
      "nextScheduleDate": "string | null"
    }
  ]
}
```

### PUT /api/v1/home/cards/pin
클럽 핀 설정 (최대 2개)

**Request:**
```json
{
  "clubIds": ["clubId1", "clubId2"]
}
```

---

## 테스트 목록

### 1. 조회 API 테스트

- [x] 핀된 클럽이 없으면 빈 배열 반환
- [x] 핀된 클럽이 1개면 1개 카드 반환
- [x] 핀된 클럽이 2개면 2개 카드 반환
- [x] 카드에 클럽 기본 정보(id, name, emblemUrl, sport, location) 포함
- [x] 카드에 사용자 역할(userRole) 포함
- [x] 카드에 멤버 수(memberCount) 포함
- [x] 카드에 배경색(backgroundColor) 포함
- [x] 카드에 다음 일정(nextScheduleDate) 포함 - 일정 있는 경우
- [x] 다음 일정이 없으면 nextScheduleDate는 null

### 2. 핀 설정 API 테스트
**의존**: Phase 1 완료 필요

- [x] 클럽 1개 핀 설정 성공
- [x] 클럽 2개 핀 설정 성공
- [x] 3개 이상 핀 설정 시 실패 (최대 2개 제한)
- [x] 핀 순서가 요청 순서대로 유지됨
- [x] 기존 핀을 새로운 핀으로 교체
- [x] 빈 배열로 모든 핀 해제
- [x] 가입하지 않은 클럽은 핀 설정 불가

### 3. 배경색 설정 테스트
**의존**: Phase 1-2 완료 필요

- [ ] 클럽별 배경색 설정 가능
- [ ] 배경색 미설정 시 기본값 사용

---

## 구현 순서

1. Domain 레이어
   - [ ] HomeCard 도메인 모델
   - [ ] HomeCardRepository 인터페이스

2. Infrastructure 레이어
   - [ ] JooqHomeCardRepository 구현

3. API 레이어
   - [ ] HomeCardApi 컨트롤러
   - [ ] DTO 클래스들

---

## 참고
- 프론트엔드: `lockr-web/src/components/HomeView.tsx`
- TDD 방식으로 진행: Red → Green → Refactor
