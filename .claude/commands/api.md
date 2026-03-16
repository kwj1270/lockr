API 엔드포인트를 분석하거나 새로 생성합니다.

대상: $ARGUMENTS

## 사용 방법
- `list {도메인}`: 해당 도메인의 API 목록 조회 (예: `list club`)
- `new {HTTP메서드} {경로}`: 새 API 엔드포인트 생성 (예: `new POST /api/v1/clubs/{clubId}/schedules`)

## API 생성 시 체크리스트
1. CQRS 패턴 확인
   - GET (순수 조회) → QueryApi + jOOQ DAO 직접 사용
   - POST/PUT/DELETE → Api + UseCase + Service + Repository
2. Request/Response DTO 생성
3. 관련 도메인 컨텍스트 확인
4. 테스트 작성 (TDD)

## 참고 파일
- Command API 예시: `domain/club/club/api/ClubApi.java`
- Query API 예시: `domain/club/club/api/ClubQueryApi.java`