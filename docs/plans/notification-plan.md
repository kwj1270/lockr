# Notification Domain Plan

## Overview
알림(Notification) 도메인의 TDD 기반 개발 계획

## 관련 파일
- Domain: `src/main/java/com/official/lockr/domain/notification/domain/Notification.java`
- Repository Interface: `src/main/java/com/official/lockr/domain/notification/domain/NotificationRepository.java`
- Service: `src/main/java/com/official/lockr/domain/notification/application/NotificationService.java`
- Infrastructure: `src/main/java/com/official/lockr/domain/notification/infrastructure/InMemoryNotificationRepository.java`
- API: `src/main/java/com/official/lockr/domain/notification/api/NotificationApi.java`
- Test: `src/test/java/com/official/lockr/domain/notification/`
- Context: `docs/domains/notification-context.md`

---

## Tasks

### Phase 1: Domain Layer - Notification

- [x] `shouldCreateNotification` - Notification.create()로 알림 생성 시 기본값 검증
- [x] `shouldMarkNotificationAsRead` - markAsRead() 호출 시 isRead=true, updatedAt 갱신

### Phase 2: Repository Layer - InMemoryNotificationRepository
**의존**: Phase 1 완료 필요

- [x] `shouldSaveAndFindNotificationById` - 알림 저장 후 ID로 조회
- [x] `shouldFindNotificationsByUserId` - 사용자 ID로 알림 목록 조회
- [x] `shouldFindNotificationsByUserIdAndClubId` - 사용자 ID + 클럽 ID로 알림 목록 조회
- [x] `shouldUpdateNotificationOnSave` - 기존 알림 업데이트 시 덮어쓰기

### Phase 3: Service Layer - NotificationService
**의존**: Phase 1-2 완료 필요

- [x] `shouldGetUserNotifications` - 사용자 알림 목록 조회
- [x] `shouldGetClubNotifications` - 클럽별 알림 목록 조회
- [x] `shouldMarkNotificationAsRead` - 알림 읽음 처리
- [x] `shouldThrowExceptionWhenNotificationNotFound` - 존재하지 않는 알림 읽음 처리 시 예외
- [x] `shouldCreateScheduleLinkNotification` - 일정 링크 요청 알림 생성
- [x] `shouldNotCreateNotificationWhenTargetClubNotExists` - 대상 클럽이 없으면 알림 생성 안함

### Phase 4: API Layer - NotificationApi (Optional)
**의존**: Phase 1-3 완료 필요

- [ ] `shouldGetNotifications` - GET /api/v1/notifications
- [ ] `shouldGetClubNotifications` - GET /api/v1/clubs/{clubId}/notifications
- [ ] `shouldMarkAsRead` - POST /api/v1/notifications/{notificationId}/read

---

## 완료 기준
- [ ] 표시: 미완료
- [x] 표시: 완료

"go" 명령 시 첫 번째 미완료 테스트부터 순서대로 진행

## Progress Log

| Date | Task | Status |
|------|------|--------|
| 2026-01-15 | shouldCreateNotification | ✅ |
| 2026-01-15 | shouldMarkNotificationAsRead | ✅ |
| 2026-01-15 | shouldSaveAndFindNotificationById | ✅ |
| 2026-01-15 | shouldFindNotificationsByUserId | ✅ |
| 2026-01-15 | shouldFindNotificationsByUserIdAndClubId | ✅ |
| 2026-01-15 | shouldUpdateNotificationOnSave | ✅ |
| 2026-01-15 | shouldGetUserNotifications | ✅ |
| 2026-01-15 | shouldGetClubNotifications | ✅ |
| 2026-01-15 | shouldMarkNotificationAsRead (Service) | ✅ |
| 2026-01-15 | shouldThrowExceptionWhenNotificationNotFound | ✅ |
| 2026-01-15 | shouldCreateScheduleLinkNotification | ✅ |
| 2026-01-15 | shouldNotCreateNotificationWhenTargetClubNotExists | ✅ |
