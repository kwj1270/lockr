# ADR-0001: 회비 도메인 Aggregate 분리 (FeePolicy / FeeRecord)

- **상태**: 승인됨
- **날짜**: 2026-04-04
- **관련 도메인**: domain/club/fee

## 컨텍스트

클럽 회비 관리 기능을 추가하면서 도메인 모델의 Aggregate 경계를 결정해야 한다. 회비 관련 데이터는 크게 두 종류다:

1. **회비 정책 (FeePolicy)**: 클럽당 1개. 월 회비 금액, 납부 기한, 계좌 정보. 거의 변경되지 않음.
2. **납부 기록 (FeeRecord)**: 멤버 x 월 수만큼 생성. 매월 전체 멤버에 대해 납부 상태(PAID/UNPAID)가 변경됨.

기존 프로젝트에서 Club-Member는 하나의 Aggregate로 묶여 있다. 회비도 같은 패턴을 따를 수 있는지 검토가 필요하다.

## 고려한 선택지

### 선택지 1: 단일 Aggregate (FeePolicy 안에 FeeRecord 포함)

Club-Member 패턴처럼 FeePolicy가 Aggregate Root, FeeRecord가 내부 Entity.

- 장점: 정책과 납부 기록의 일관성을 하나의 트랜잭션에서 보장
- 장점: Repository 1개로 관리 단순
- 단점: **Aggregate 크기가 지속적으로 증가** — 20명 클럽 기준 1년이면 240개 자식 Entity. Club-Member(~20개 고정)와 규모가 다름
- 단점: **동시성 충돌** — 총무가 여러 회원의 납부 상태를 동시에 변경할 때, 같은 Aggregate를 잠가야 함
- 단점: 정책 변경과 납부 상태 변경이 같은 트랜잭션에 묶여 불필요한 결합

### 선택지 2: 별도 Aggregate (FeePolicy + FeeRecord 분리)

FeePolicy와 FeeRecord를 각각 독립 Aggregate Root로 설계. FeeRecord는 clubId로 FeePolicy를 참조.

- 장점: **독립적 생명주기** — Record는 매월 생성, Policy와 무관하게 변경
- 장점: **동시성 자유** — 회원별 Record가 독립적이므로 동시 변경에 충돌 없음
- 장점: **Aggregate 크기 일정** — 각 FeeRecord는 단일 엔티티, 비대해지지 않음
- 단점: Repository 2개 필요 (FeePolicyRepository + FeeRecordRepository)
- 단점: "정책이 없으면 Record를 생성할 수 없다"는 규칙을 Application Layer에서 검증해야 함

## 결정

**선택지 2를 채택한다.** FeePolicy와 FeeRecord를 별도 Aggregate Root로 분리한다.

## 근거

Club-Member 패턴과의 핵심 차이점이 분리를 정당화한다:

| | Club ↔ Member | FeePolicy ↔ FeeRecord |
|--|--------------|----------------------|
| 자식 수 | ~20개 (고정적) | 멤버 x 월수 (지속 증가) |
| 변경 빈도 | 드묾 (가입/탈퇴) | 매월 전원 변경 |
| 동시 변경 | 드묾 | 총무가 여러 명 동시 처리 |
| 최종 일관성 허용 | 아니오 (멤버 추가는 즉시) | 예 (납부 확인은 비동기 OK) |

DDD의 Aggregate 경계 판단 기준을 적용하면:
- "한 트랜잭션에서 일관성이 필요한가?" → 정책 변경과 납부 상태 변경은 동시에 일어나지 않음
- "최종 일관성으로 충분한가?" → 납부 기록은 정책과 최종 일관성으로 충분
- "ID 참조만으로 충분한가?" → FeeRecord는 clubId만으로 정책 참조 가능
- "Aggregate 내 Entity가 10개 이상인가?" → 1년이면 240개 이상

모든 기준이 "분리"를 가리킨다.

## 결과

- 긍정: 각 Aggregate가 작고 독립적이어 성능과 동시성에 유리
- 긍정: 납부 기록 조회/변경 시 정책 데이터를 불필요하게 로드하지 않음
- 부정: "정책 미설정 시 Record 생성 불가" 규칙을 Service에서 검증해야 함 (FeeService.updateRecord()에서 FeePolicy 존재 여부 확인)
- 부정: Repository가 2개로 증가 (FeePolicyRepository + FeeRecordRepository)
- 주의: 정책 삭제/변경 시 기존 Record에 영향을 주지 않도록 설계 (Record는 생성 시점의 금액을 기록하지 않고, 정책의 현재 금액을 참조)
