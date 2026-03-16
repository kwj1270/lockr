# Home Domain Context

홈 화면 Bounded Context

## 서브 도메인

### Card (홈 카드)

사용자가 핀(고정)한 클럽을 카드 형태로 표시하는 기능

#### 주요 기능

##### 카드 조회 (GET /api/v1/home/cards)
핀된 클럽 카드 목록 조회

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
      "memberCount": "number",
      "location": "string",
      "nextScheduleDate": "string | null"
    }
  ]
}
```

##### 핀 설정 (PUT /api/v1/home/cards/pin)
클럽 핀 설정 (최대 2개)

```json
{
  "clubIds": ["clubId1", "clubId2"]
}
```

## 비즈니스 규칙
1. 최대 2개까지 핀 설정 가능
2. 가입한 클럽만 핀 설정 가능
3. 핀 순서는 요청 순서대로 유지
4. 빈 배열로 모든 핀 해제 가능

## 의존 관계
- `Club` → 클럽 정보 조회
- `Schedule` → 다음 일정 조회
- `Member` → 사용자 역할 확인

## 특징
- Query 전용 도메인 (읽기 최적화)
- 핀 설정은 사용자별 설정 저장
