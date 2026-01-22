테스트를 실행합니다.

대상: $ARGUMENTS

## 실행 방식
- 인자가 없으면: 전체 테스트 실행 (`./gradlew test`)
- 도메인 이름이면: 해당 도메인 테스트 실행 (예: `schedule` → `./gradlew test --tests "*Schedule*"`)
- 클래스 이름이면: 해당 클래스 테스트 실행 (예: `ScheduleTest` → `./gradlew test --tests "ScheduleTest"`)
- 메서드 패턴이면: 해당 메서드 테스트 실행 (예: `ScheduleTest.shouldCreate` → `./gradlew test --tests "*ScheduleTest.shouldCreate*"`)

## 결과 보고
- 테스트 성공/실패 여부
- 실패한 테스트가 있으면 실패 원인 분석
- 수정 제안 (필요시)