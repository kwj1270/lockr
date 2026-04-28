# Club Bounded Context

가장 큰 BC. 8개 subdomain, 13 Aggregate Root, 18 Domain Event.

## Subdomain Map

| Subdomain | Aggregate Root | 핵심 역할 |
|-----------|---------------|----------|
| club | Club | 클럽 생성/관리, Member(VO) 컬렉션 |
| chat | Chat, ChatRoom | SSE 기반 실시간 채팅, 메시지 고정 |
| feed | Feed | 게시글, 댓글, 좋아요, 이미지/영상 |
| schedule | Schedule | 일정 생성/참석, 출석 상태 관리 |
| recruitment | Recruitment, Application | 모집 공고, 가입 신청/승인/거절 |
| sport/football | Lineup, Squad | 포메이션 배치, 선수단 관리 |
| stats | MatchRecord | 경기 기록, 선수 퍼포먼스, 시즌 통계 |
| fee | FeePolicy, FeeRecord, FeeNotification | 회비 정책, 납부 기록, 미납 알림 |

## Cross-Subdomain Event Flow

publisher ≠ consumer인 경계 횡단 이벤트만 기록.

| Event | Publisher | Consumer(s) | 효과 |
|-------|----------|------------|------|
| FoundClubEvent | club | chat, squad, feed | 채팅방 생성, 스쿼드 초기화, 피드 생성 |
| AddedClubMemberEvent | club | chat, squad | 채터 추가, 스쿼드 선수 추가 |
| RemovedClubMemberEvent | club | chat, squad | 채터 제거, 스쿼드 선수 제거 |
| CreatedScheduleEvent | schedule | feed, notification | SCHEDULE 피드 생성, 푸시 알림 |
| UpdatedScheduleEvent | schedule | feed | 피드 메타데이터 갱신 |
| CancelledScheduleEvent | schedule | feed | 피드 상태 갱신 |
| ApprovedApplicationEvent | recruitment | club | 클럽 멤버 추가 |
| FeePolicyChangedEvent    | fee | (없음, 내부 기록용) | 회비 정책 변경 기록 |
| FeeRecordMarkedPaidEvent | fee | (없음, 내부 기록용) | 납부 완료 기록 |
| UnpaidFeeNotifiedEvent   | fee | notification | 미납 회원에게 푸시 알림 [Integration·Outbox] |

> [Integration·Outbox] 표시 이벤트는 `IntegrationDomainEvent`로, `domain_event_outbox`에 영속 후
> `OutboxProcessor`가 폴링하여 `ApplicationEventPublisher`로 재발행. Consumer는 `IdempotentEventHandler`
> 상속만으로 충분. `@EventListener` / `@Transactional` boilerplate 불필요 — 베이스가 `ApplicationListener`로
> 진입점을 직접 소유 (ADR-0008). 그 외 이벤트는 in-process sync.

## Cross-Domain Dependencies

| 외부 도메인 | 방향 | Event/참조 | 설명 |
|------------|------|-----------|------|
| notification | club → notification | Schedule 이벤트 | 일정 알림 발송 |
| notification | fee → notification | UnpaidFeeNotifiedEvent | 미납 회비 알림 발송 |
| users | users → club (간접) | WithdrawnUserEvent | 탈퇴 시 멤버 정리 가능 |

## 비즈니스 규칙

### Club
| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| removeMember | 운영진은 탈퇴 불가, 먼저 역할 해제 | IllegalStateException |
| kickMember | 회장/부회장만 강퇴 가능, 대상 멤버 존재 필수, 자기 자신 강퇴 불가 | IllegalArgumentException |
| delegatePresident | 회장만 위임 가능, 대상은 클럽 멤버여야 함 | IllegalArgumentException |
| changeMemberRole | 회장/부회장만 변경 가능, 회장 역할은 변경 불가 | IllegalArgumentException |
| changeVisibility/changeJoinMethod/updateInfo | 회장/부회장만 가능 | IllegalArgumentException |

### Schedule
| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| validateScheduleTime | 일정 시간 필수, 미래여야 함 | IllegalArgumentException |
| validateMinParticipants | 최소 참가자 음수 불가 | IllegalArgumentException |
| validateNotCancelled | 취소된 일정 수정 불가 | IllegalStateException |
| addMember | 이미 초대된 사용자 중복 불가 | IllegalArgumentException |

### Feed
| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| create | 피드 내용 필수 | IllegalArgumentException |
| update | 작성자만 수정 가능, 삭제된 피드 수정 불가 | IllegalStateException |
| delete | 작성자 또는 운영진만 삭제 가능 | IllegalStateException |
| addHeart | 삭제된 피드 좋아요 불가, 중복 좋아요 불가 | IllegalStateException |
| addComment | 삭제된 피드 댓글 불가 | IllegalStateException |

### Fee
| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| validateAmount | 회비 금액 0원 이상 | IllegalArgumentException |
| validateDueDay | 납부 기한 1~28일 | IllegalArgumentException |
| FeeRecord.markPaid | 이미 PAID면 early return (멱등) | — |
| FeeRecord.markDeferred | UNPAID→DEFERRED만 허용. PAID→DEFERRED는 IllegalStateException. DEFERRED→DEFERRED는 early return (멱등) | IllegalStateException |
| notifyUnpaid | 같은 월 미납 알림 최대 3회 | IllegalStateException |

#### FeeRecord 상태 전이 정책

| 전이 | 메서드 | 허용 여부 |
|------|--------|----------|
| UNPAID → PAID | markPaid | 허용 |
| UNPAID → DEFERRED | markDeferred | 허용 |
| DEFERRED → PAID | markPaid | 허용 |
| DEFERRED → UNPAID | markUnpaid | 허용 |
| PAID → UNPAID | markUnpaid | 허용 |
| PAID → DEFERRED | markDeferred | 불허 (IllegalStateException) |

#### DEFERRED 알림 정책

- 유예(DEFERRED) 상태 멤버는 `notifyUnpaid` 발송 대상에서 제외 (UNPAID만 필터링하므로 자동 제외).
- 납부율 계산(`FeeQueryApi`)에서 DEFERRED 멤버는 PAID로 카운트되지 않음 (미납으로 처리).

### ChatRoom
| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| addChatter | 이미 참여 중인 사용자 중복 불가 | IllegalArgumentException |

## Gotchas

- Club.Member는 AR이 아닌 VO — Club 내부 컬렉션으로만 관리
- Lineup 슬롯 배치는 4가지 케이스 (빈슬롯+새선수, 기존슬롯+새선수, 빈슬롯+기존선수, 기존슬롯+기존선수)
- Chat의 clubId는 이벤트 리스너 레벨에서 해결 (Chat AR에는 chatRoomId만 보유)

## Reference

- 이벤트 다이어그램: `docs/models/event-flow.md`
- Aggregate 관계도: `docs/models/class-diagrams/_overview.md`
