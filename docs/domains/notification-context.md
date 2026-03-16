# Notification Domain Context

알림 Bounded Context

## Aggregate Root

### Notification
```
Notification
├── id: String
├── userId: String (수신자)
├── clubId: String (관련 클럽)
├── type: NotificationType
├── title: String
├── content: String
├── data: JsonNode (추가 데이터)
├── isRead: boolean
├── actionType: String
├── actionUrl: String
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime
```

## NotificationType (Enum)
- `SCHEDULE_LINK_REQUEST`: 일정 연계 요청
- `SCHEDULE_UPDATED`: 일정 업데이트
- `SCHEDULE_CANCELLED`: 일정 취소

## 주요 기능

### 알림 생성
- `Notification.init(...)`: 새 알림 생성 및 `CreatedNotificationEvent` 발행
- `isRead`: 기본값 false

### 알림 읽음 처리
- `markAsRead()`: isRead를 true로 변경
- `readAll(ReadAllNotificationsCommand)`: 사용자의 모든 알림을 읽음 처리

### 알림 삭제
- `softDelete()`: deletedAt 설정 (soft delete)
- `deleteAll(DeleteAllNotificationsCommand)`: 사용자의 모든 알림 soft delete

### 알림 조회
- 사용자별 알림 목록 조회
- 클럽별 알림 목록 조회
- `UnreadCountResponse(count)`: 읽지 않은 알림 수 조회

## Command 목록
- `MarkAsReadNotificationCommand(notificationId)`
- `CreateScheduleLinkNotificationCommand`
- `DeleteNotificationCommand(notificationId)`
- `DeleteAllNotificationsCommand(userId)`
- `ReadAllNotificationsCommand(userId)`

## Repository 인터페이스
```
NotificationRepository
├── save(notification)
├── findById(id)
├── findByUserId(userId)
├── findByUserIdAndClubId(userId, clubId)
├── softDeleteAllByUserId(userId)
└── readAllByUserId(userId)
```

## 비즈니스 규칙
1. 알림은 특정 사용자에게 전송됨
2. 클럽 관련 알림은 clubId를 가짐
3. actionUrl로 알림 클릭 시 이동할 경로 지정
4. 삭제는 soft delete 방식으로 처리

## 도메인 이벤트
- `CreatedNotificationEvent(id, userId, clubId, type, title, createdAt)`: 알림 생성 시 발행

## 의존 관계 (이벤트 구독)
- `Schedule.CreatedScheduleEvent` → 일정 생성 알림
- `Schedule.CancelledScheduleEvent` → 일정 취소 알림
- `Club.AddedClubMemberEvent` → 멤버 가입 알림
- `Recruitment.Application` → 지원서 알림

## 확장 계획
- 푸시 알림 연동
- 이메일 알림
- 알림 설정 (타입별 on/off)
