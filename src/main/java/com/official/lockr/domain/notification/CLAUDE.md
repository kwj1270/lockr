# Notification Bounded Context

푸시 알림 도메인. FCM 토큰 관리, 알림 생성/읽음/삭제.

## Aggregate

| 타입 | 이름 | 핵심 역할 |
|------|------|----------|
| AR | Notification | 알림 생성 (isRead=false), markAsRead(), soft delete |

## Domain Events

| Event | 설명 |
|-------|------|
| CreatedNotificationEvent | 알림 생성 시 발행 |

## Repository

- **NotificationRepository**: save, findById, findByUserId, softDeleteAllByUserId, readAllByUserId
- **FcmTokenRepository**: userId별 다중 디바이스 토큰 관리

## Cross-Domain Dependencies

| 외부 도메인 | 방향 | Event | 설명 |
|------------|------|-------|------|
| club | club → notification | Schedule 이벤트 | 일정 생성/변경 시 푸시 알림 발송 |
| club | fee → notification | UnpaidFeeNotifiedEvent (Integration·Outbox) | 미납 회비 푸시 알림 발송 (CreateFeeUnpaidNotificationUseCase) |

## 비즈니스 규칙

| 대상 | 규칙 |
|------|------|
| Notification 생성 | isRead=false 초기값 |
| markAsRead() | isRead=true + 타임스탬프 갱신 |
| soft delete | deletedAt 타임스탬프 방식 |
