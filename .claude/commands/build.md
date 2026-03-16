프로젝트를 빌드합니다.

옵션: $ARGUMENTS

## 빌드 옵션
- (인자 없음): 전체 빌드 (`./gradlew build`)
- `fast`: 테스트 제외 빌드 (`./gradlew build -x test`)
- `clean`: 클린 빌드 (`./gradlew clean build`)
- `jooq`: jOOQ 코드 생성 (`./gradlew generateJooq`)

## 결과 보고
- 빌드 성공/실패 여부
- 실패 시 에러 원인 분석
- 컴파일 에러가 있으면 수정 제안