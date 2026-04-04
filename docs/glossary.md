# Domain Glossary

> Auto-generated (자동 추출 섹션) + 수동 보완 섹션으로 구성.
> 자동 섹션은 `bash .claude/scripts/generate-domain-docs.sh` 로 재생성.

---

## Aggregate Roots

| Aggregate | 파일 경로 |
|-----------|----------|
| Application | `src/main/java/com/official/lockr/domain/club/recruitment/applications/domain/Application.java` |
| Chat | `src/main/java/com/official/lockr/domain/club/chat/domain/Chat.java` |
| ChatRoom | `src/main/java/com/official/lockr/domain/club/chat/domain/ChatRoom.java` |
| Club | `src/main/java/com/official/lockr/domain/club/club/domain/Club.java` |
| FeePolicy | `src/main/java/com/official/lockr/domain/club/fee/domain/FeePolicy.java` |
| FeeRecord | `src/main/java/com/official/lockr/domain/club/fee/domain/FeeRecord.java` |
| Feed | `src/main/java/com/official/lockr/domain/club/feed/domain/Feed.java` |
| Lineup | `src/main/java/com/official/lockr/domain/club/sport/football/lineup/domain/Lineup.java` |
| MatchRecord | `src/main/java/com/official/lockr/domain/club/stats/domain/MatchRecord.java` |
| Notification | `src/main/java/com/official/lockr/domain/notification/domain/Notification.java` |
| Recruitment | `src/main/java/com/official/lockr/domain/club/recruitment/recruitment/domain/Recruitment.java` |
| Schedule | `src/main/java/com/official/lockr/domain/club/schedule/domain/Schedule.java` |
| Shorts | `src/main/java/com/official/lockr/domain/shorts/domain/Shorts.java` |
| SignIn | `src/main/java/com/official/lockr/domain/auth/signin/domain/SignIn.java` |
| Squad | `src/main/java/com/official/lockr/domain/club/sport/football/squad/domain/Squad.java` |
| Users | `src/main/java/com/official/lockr/domain/users/domain/Users.java` |

## Domain Events

| Event | 설명 |
|-------|------|
| AddedClubMemberEvent | |
| ApprovedApplicationEvent | |
| AttendanceStatusChangedEvent | |
| CancelledScheduleEvent | |
| CreatedChatEvent | |
| CreatedChatRoomEvent | |
| CreatedNotificationEvent | |
| CreatedScheduleEvent | |
| DeletedMatchEvent | |
| FeePolicyChangedEvent | |
| FeeRecordMarkedPaidEvent | |
| FoundClubEvent | |
| ProcessedSignInEvent | |
| RecordedMatchEvent | |
| RejectedApplicationEvent | |
| RemovedChatterEvent | |
| RemovedClubMemberEvent | |
| UnpaidFeeNotifiedEvent | |
| UpdatedMatchEvent | |
| UpdatedScheduleEvent | |
| UploadedShortsEvent | |
| WithdrawnUserEvent | |

## Enums

| Enum | 값 목록 |
|------|--------|
| ApplicationFormType | SIMPLE,DETAILED |
| ApplicationStatus | SUBMITTED,APPROVED,REJECTED,CANCELED |
| AttendanceStatus | ATTENDING,NOT_ATTENDING,NO_RESPONSE,A |
| ChatSseEventType | ROOM_CREATED,ROOM_UPDATED,NEW_MESSAGE,MESSAGE_UPDATED,MESSAGE_DELETED,MESSAGE_PINNED,MESSAGE_UNPINNED,CHATTER_JOINED,CHATTER_LEFT |
| Day | SUNDAY,MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY |
| FeeStatus | UNPAID,PAID |
| FeedType | GENERAL,NOTICE,SCHEDULE |
| MatchResult | WIN,DRAW,LOSE |
| MemberRole | BASIC,COACH,MANAGER,TREASURER,VICE_PRESIDENT,PRESIDENT |
| ModerationStatus | ACTIVE,HIDDEN |
| NotificationType | SCHEDULE_LINK_REQUEST,SCHEDULE_UPDATED,SCHEDULE_CANCELLED |
| ProviderType | GOOGLE,APPLE |
| RecruitmentStatus | RECRUITING,PAUSED |
| RecruitmentType | SIMPLE,DETAILED |
| ReportReason | INAPPROPRIATE_CONTENT,SPAM,HARASSMENT,OTHER |
| ScheduleStatus | SCHEDULED,IN_PROGRESS,COMPLETED,CANCELLED,S |
| ScheduleType | MATCH,TRAINING,SOCIAL_EVENT |
| SportType | FOOT_BALL |

## Value Objects

| Value Object | 파일 경로 |
|--------------|----------|
| Admin | `src/main/java/com/official/lockr/domain/auth/admin/domain/Admin.java` |
| ApplicationFormData | `src/main/java/com/official/lockr/domain/club/recruitment/applications/domain/vo/form/ApplicationFormData.java` |
| Attendance | `src/main/java/com/official/lockr/domain/club/schedule/domain/Attendance.java` |
| BankAccount | `src/main/java/com/official/lockr/domain/club/fee/domain/BankAccount.java` |
| ChatSseEvent | `src/main/java/com/official/lockr/domain/club/chat/domain/event/ChatSseEvent.java` |
| Chatter | `src/main/java/com/official/lockr/domain/club/chat/domain/Chatter.java` |
| Comment | `src/main/java/com/official/lockr/domain/club/feed/domain/comment/Comment.java` |
| CommentHeart | `src/main/java/com/official/lockr/domain/club/feed/domain/comment/CommentHeart.java` |
| CommentHearts | `src/main/java/com/official/lockr/domain/club/feed/domain/comment/CommentHearts.java` |
| CommentImages | `src/main/java/com/official/lockr/domain/club/feed/domain/comment/CommentImages.java` |
| CommentVideos | `src/main/java/com/official/lockr/domain/club/feed/domain/comment/CommentVideos.java` |
| Days | `src/main/java/com/official/lockr/domain/club/recruitment/recruitment/domain/vo/Days.java` |
| FcmToken | `src/main/java/com/official/lockr/domain/notification/domain/FcmToken.java` |
| FeedImages | `src/main/java/com/official/lockr/domain/club/feed/domain/FeedImages.java` |
| FeedVideos | `src/main/java/com/official/lockr/domain/club/feed/domain/FeedVideos.java` |
| FootballSportSpecificData | `src/main/java/com/official/lockr/domain/club/recruitment/applications/domain/vo/sport/FootballSportSpecificData.java` |
| Heart | `src/main/java/com/official/lockr/domain/club/feed/domain/entity/Heart.java` |
| HomeCardApi | `src/main/java/com/official/lockr/domain/home/card/HomeCardApi.java` |
| HomeNoticeApi | `src/main/java/com/official/lockr/domain/home/notice/HomeNoticeApi.java` |
| HomeScheduleApi | `src/main/java/com/official/lockr/domain/home/schedule/HomeScheduleApi.java` |
| Image | `src/main/java/com/official/lockr/domain/club/feed/domain/entity/Image.java` |
| LineupSlot | `src/main/java/com/official/lockr/domain/club/sport/football/lineup/domain/LineupSlot.java` |
| LineupSlots | `src/main/java/com/official/lockr/domain/club/sport/football/lineup/domain/LineupSlots.java` |
| MatchDetailData | `src/main/java/com/official/lockr/domain/club/schedule/domain/vo/MatchDetailData.java` |
| MatchScore | `src/main/java/com/official/lockr/domain/club/stats/domain/MatchScore.java` |
| Member | `src/main/java/com/official/lockr/domain/club/club/domain/Member.java` |
| Oidc | `src/main/java/com/official/lockr/domain/auth/oidc/domain/Oidc.java` |
| OidcPublicKey | `src/main/java/com/official/lockr/domain/auth/oidc/domain/vo/OidcPublicKey.java` |
| OidcPublicKeyId | `src/main/java/com/official/lockr/domain/auth/oidc/domain/vo/OidcPublicKeyId.java` |
| OidcPublicKeys | `src/main/java/com/official/lockr/domain/auth/oidc/domain/vo/OidcPublicKeys.java` |
| PinnedMessage | `src/main/java/com/official/lockr/domain/club/chat/domain/PinnedMessage.java` |
| PlayerPerformance | `src/main/java/com/official/lockr/domain/club/stats/domain/PlayerPerformance.java` |
| ProcessingInfo | `src/main/java/com/official/lockr/domain/club/recruitment/applications/domain/vo/ProcessingInfo.java` |
| ScheduleAttendanceHistory | `src/main/java/com/official/lockr/domain/club/schedule/domain/ScheduleAttendanceHistory.java` |
| ShortsComment | `src/main/java/com/official/lockr/domain/shorts/domain/ShortsComment.java` |
| ShortsHeart | `src/main/java/com/official/lockr/domain/shorts/domain/ShortsHeart.java` |
| ShortsMember | `src/main/java/com/official/lockr/domain/shorts/domain/ShortsMember.java` |
| ShortsReport | `src/main/java/com/official/lockr/domain/shorts/domain/ShortsReport.java` |
| SignInSession | `src/main/java/com/official/lockr/domain/auth/signin/domain/SignInSession.java` |
| SignInToken | `src/main/java/com/official/lockr/domain/auth/signin/domain/SignInToken.java` |
| SquadPlayer | `src/main/java/com/official/lockr/domain/club/sport/football/squad/domain/SquadPlayer.java` |
| UserAdditionalInfo | `src/main/java/com/official/lockr/domain/users/domain/UserAdditionalInfo.java` |
| Video | `src/main/java/com/official/lockr/domain/club/feed/domain/entity/Video.java` |

---

## 수동 보완 섹션

> 아래 테이블은 자동 추출되지 않는 개념 정의를 수동으로 기입한다.

### 유비쿼터스 언어 (Ubiquitous Language)

| 용어 | 설명 |
|------|------|
| | |

### 바운디드 컨텍스트 경계

| 컨텍스트 | 책임 |
|----------|------|
| | |
