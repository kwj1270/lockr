# TDD Plan: 클럽 회비 관리

> **PRD**: `../docs/specs/14-club-fee.md`
> **Feature Tracker**: `../docs/features/club-fee.md`
> **도메인**: `domain/club/fee/`
> **생성일**: 2026-04-04
> **상태**: 진행 중

---

## 구현 대상

| # | 기능 | 유형 | 상태 |
|---|------|------|------|
| 1 | FeePolicy Aggregate Root | Domain | 대기 |
| 2 | FeeRecord Aggregate Root | Domain | 대기 |
| 3 | FeePolicy UseCase + Service | Application | 대기 |
| 4 | FeeRecord UseCase + Service | Application | 대기 |
| 5 | JOOQ Repository 구현 | Infrastructure | 대기 |
| 6 | Flyway Migration | Infrastructure | 대기 |
| 7 | FeeApi (Command) | API | 대기 |
| 8 | FeeQueryApi (Query) | API | 대기 |

---

## Phase 1: Domain — FeePolicy

- [ ] FeePolicy.init()으로 생성하면 id(ULID), clubId, amount, dueDay가 설정되어야 한다
- [ ] FeePolicy.init()으로 생성하면 FeePolicyChangedEvent가 발행되어야 한다
- [ ] dueDay가 1 미만이면 IllegalArgumentException이 발생해야 한다
- [ ] dueDay가 28 초과이면 IllegalArgumentException이 발생해야 한다
- [ ] amount가 0 미만이면 IllegalArgumentException이 발생해야 한다
- [ ] amount가 0이면 유효한 정책으로 생성되어야 한다 (회비 없음)
- [ ] updatePolicy()로 금액/기한/계좌를 변경하면 FeePolicyChangedEvent가 발행되어야 한다
- [ ] BankAccount VO는 bankName, accountNumber, accountHolder를 가지며, 모두 null이면 null 반환해야 한다
- [ ] equals/hashCode는 id 기반이어야 한다

## Phase 2: Domain — FeeRecord

**의존**: Phase 1 완료 필요

- [ ] FeeRecord.create()로 생성하면 status가 UNPAID이어야 한다
- [ ] FeeRecord.create()로 생성하면 id(ULID), clubId, memberId, year, month가 설정되어야 한다
- [ ] markPaid()를 호출하면 status가 PAID로 변경되어야 한다
- [ ] markPaid()를 호출하면 FeeRecordMarkedPaidEvent가 발행되어야 한다
- [ ] markPaid()시 updatedBy가 기록되어야 한다
- [ ] markUnpaid()를 호출하면 status가 UNPAID로 변경되어야 한다
- [ ] 이미 PAID인 상태에서 markPaid()를 호출하면 중복 이벤트가 발행되지 않아야 한다
- [ ] memo를 설정하면 저장되어야 한다
- [ ] equals/hashCode는 id 기반이어야 한다

## Phase 3: Domain — Domain Events

**의존**: Phase 1, 2 완료 필요

- [ ] FeePolicyChangedEvent는 policyId, clubId, amount, dueDay를 포함해야 한다
- [ ] FeeRecordMarkedPaidEvent는 recordId, clubId, memberId, year, month를 포함해야 한다
- [ ] UnpaidFeeNotifiedEvent는 clubId, year, month, notifiedMemberIds를 포함해야 한다
- [ ] 모든 이벤트는 DomainEvent를 implements 하는 record여야 한다

## Phase 4: Application — FeePolicy UseCase

**의존**: Phase 1~3 완료 필요

- [ ] SetFeePolicyUseCase 인터페이스에 setPolicy(SetFeePolicyCommand) 메서드가 있어야 한다
- [ ] SetFeePolicyCommand는 clubId, userId, amount, dueDay, bankName?, accountNumber?, accountHolder?를 가져야 한다
- [ ] FeeService.setPolicy()는 기존 정책이 없으면 FeePolicy.init()으로 생성해야 한다
- [ ] FeeService.setPolicy()는 기존 정책이 있으면 updatePolicy()로 수정해야 한다
- [ ] FeeService.setPolicy()는 userId가 운영진(PRESIDENT, VICE_PRESIDENT, TREASURER)이 아니면 예외를 던져야 한다

## Phase 5: Application — FeeRecord UseCase

**의존**: Phase 4 완료 필요

- [ ] UpdateFeeRecordUseCase 인터페이스에 updateRecord(UpdateFeeRecordCommand) 메서드가 있어야 한다
- [ ] UpdateFeeRecordCommand는 clubId, userId, memberId, year, month, status, memo?를 가져야 한다
- [ ] FeeService.updateRecord()는 해당 월의 FeeRecord가 없으면 FeeRecord.create() 후 상태를 변경해야 한다
- [ ] FeeService.updateRecord()는 해당 월의 FeeRecord가 있으면 상태를 변경해야 한다
- [ ] FeeService.updateRecord()는 FeePolicy가 없으면 IllegalStateException("회비 정책을 먼저 설정해주세요")을 던져야 한다
- [ ] NotifyUnpaidFeeUseCase 인터페이스에 notifyUnpaid(NotifyUnpaidFeeCommand) 메서드가 있어야 한다
- [ ] NotifyUnpaidFeeCommand는 clubId, userId, year, month를 가져야 한다
- [ ] FeeService.notifyUnpaid()는 해당 월 UNPAID 상태인 멤버 목록을 조회하여 UnpaidFeeNotifiedEvent를 발행해야 한다
- [ ] FeeService.notifyUnpaid()는 같은 월에 3회 초과 알림 시 예외를 던져야 한다

## Phase 6: Infrastructure — Flyway + Repository

**의존**: Phase 5 완료 필요

- [ ] V13__create_fee_tables.sql이 fee_policies, fee_records 테이블을 생성해야 한다
- [ ] fee_records에 (club_id, member_id, year, month) UNIQUE 제약이 있어야 한다
- [ ] JOOQFeePolicyRepository.save()는 upsert + 이벤트 발행을 해야 한다
- [ ] JOOQFeePolicyRepository.findByClubId()는 clubId로 정책을 조회해야 한다
- [ ] JOOQFeeRecordRepository.save()는 upsert + 이벤트 발행을 해야 한다
- [ ] JOOQFeeRecordRepository.findByClubIdAndYearAndMonth()는 해당 월 전체 레코드를 반환해야 한다

## Phase 7: API — FeeApi (Command)

**의존**: Phase 6 완료 필요

- [ ] PUT /api/v1/clubs/{clubId}/fee-policy → SetFeePolicyUseCase 호출, request.toCommand() 패턴
- [ ] PUT /api/v1/clubs/{clubId}/fee-records/{memberId} → UpdateFeeRecordUseCase 호출
- [ ] POST /api/v1/clubs/{clubId}/fee-records/notify-unpaid → NotifyUnpaidFeeUseCase 호출

## Phase 8: API — FeeQueryApi (Query)

**의존**: Phase 6 완료 필요 (Phase 7과 병렬 가능)

- [ ] GET /api/v1/clubs/{clubId}/fee-policy → Configuration DI → FeePoliciesDao.ctx() 직접 조회
- [ ] GET /api/v1/clubs/{clubId}/fee-records?year&month → 운영진이면 전체 멤버 납부 상태 + summary 반환
- [ ] GET /api/v1/clubs/{clubId}/fee-records?year&month → 일반 회원이면 본인 상태 + paidRate만 반환
- [ ] GET /api/v1/clubs/{clubId}/fee-records/me?year&month → 본인 납부 상태 + paidRate 반환
- [ ] 응답에 summary(total, paid, unpaid, paidRate)가 포함되어야 한다

---

## 참고

- tactical-design 스킬 참조하여 패키지 구조 생성
- 구현 후 `bash .claude/scripts/check-domain-test-coverage.sh`로 누락 확인
