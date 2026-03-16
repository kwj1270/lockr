# Plan - Schedule Domain TDD

## 관련 파일
- Domain: `src/main/java/com/official/lockr/domain/club/schedule/domain/Schedule.java`
- Service: `src/main/java/com/official/lockr/domain/club/schedule/application/ScheduleService.java`
- Repository: `src/main/java/com/official/lockr/domain/club/schedule/domain/ScheduleRepository.java`
- Infrastructure: `src/main/java/com/official/lockr/domain/club/schedule/infrastructure/JOOQScheduleRepository.java`
- Test: `src/test/java/com/official/lockr/domain/club/schedule/domain/ScheduleTest.java`
- Context: `docs/domains/club-context.md` (Schedule 섹션)

---

## Phase 1: Domain Entity - Schedule 기본 생성

- [x] Schedule 생성 시 필수 필드가 설정되어야 한다
- [x] Schedule 생성 시 과거 시간이면 예외가 발생해야 한다
- [x] Schedule 생성 시 CreatedScheduleEvent가 발행되어야 한다

## Phase 2: Domain Entity - ScheduleType별 생성

- [x] MATCH 타입 Schedule 생성 시 MatchDetailData가 설정되어야 한다
- [x] TRAINING 타입 Schedule 생성 시 TrainingDetailData가 설정되어야 한다
- [x] SOCIAL_EVENT 타입 Schedule 생성 시 SocialDetailData가 설정되어야 한다

## Phase 3: Domain Entity - Attendance 응답

- [x] Schedule에 응답하면 Attendance가 생성되어야 한다
- [x] 이미 응답한 사용자가 다시 응답하면 상태가 업데이트되어야 한다
- [x] 응답 시 RespondedToScheduleEvent가 발행되어야 한다
- [x] 취소된 Schedule에는 응답할 수 없어야 한다

## Phase 4: Domain Entity - Schedule 업데이트

- [x] Schedule 업데이트 시 필드가 변경되어야 한다
- [x] Schedule 업데이트 시 UpdatedScheduleEvent가 발행되어야 한다
- [x] 취소된 Schedule은 업데이트할 수 없어야 한다

## Phase 5: Domain Entity - Schedule 취소

- [x] Schedule 취소 시 상태가 CANCELLED로 변경되어야 한다
- [x] Schedule 취소 시 CancelledScheduleEvent가 발행되어야 한다
- [x] 이미 취소된 Schedule은 다시 취소할 수 없어야 한다

## Phase 6: Domain Entity - 멤버 추가 및 참석 집계

- [x] Schedule에 멤버를 추가하면 NO_RESPONSE 상태의 Attendance가 생성되어야 한다
- [x] 참석 인원 수를 정확히 집계해야 한다
- [x] 불참 인원 수를 정확히 집계해야 한다
- [x] 미응답 인원 수를 정확히 집계해야 한다

## Phase 7: Application Layer - ScheduleService
**의존**: Phase 1-6 완료 필요

- [x] RegisterScheduleUseCase: 일정 생성 시 멤버들에게 Attendance가 자동 생성되어야 한다
- [x] RespondToScheduleUseCase: 클럽 멤버만 응답할 수 있어야 한다
- [x] UpdateScheduleUseCase: 스태프만 일정을 수정할 수 있어야 한다
- [x] CancelScheduleUseCase: 스태프만 일정을 취소할 수 있어야 한다

## Phase 8: Infrastructure Layer - Repository
**의존**: Phase 1-7 완료 필요

- [x] JOOQScheduleRepository: Schedule 저장 후 조회 시 동일한 데이터가 반환되어야 한다
- [x] JOOQScheduleRepository: Attendance 변경사항이 정확히 저장되어야 한다
- [ ] JOOQScheduleRepository: ScheduleDetailData JSON 직렬화/역직렬화가 정상 동작해야 한다

---

## 완료 기준
- [ ] 표시: 미완료
- [x] 표시: 완료

"go" 명령 시 첫 번째 미완료 테스트부터 순서대로 진행
