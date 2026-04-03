# Domain Event Flow

> Auto-generated from codebase. Do not edit manually.
> Run: `bash .claude/scripts/generate-event-flow.sh`
```mermaid
flowchart LR
    %% Bounded Contexts
    subgraph Auth[Auth]
        Auth_SignIn[SignIn]
        Auth_OIDC[OIDC]
    end
    subgraph Club[Club]
        Club_Club[Club]
        Club_Schedule[Schedule]
        Club_Chat[Chat]
        Club_Feed[Feed]
        Club_Recruitment[Recruitment]
        Club_Squad[Squad]
        Club_Lineup[Lineup]
        Club_Stats[Stats]
    subgraph Others[Others]
        Users[Users]
        Notification[Notification]
        Shorts[Shorts]
    %% Event Flows
    Users -->|WithdrawnUser| Auth_SignIn
    Club_Club -->|AddedClubMember| Club_Chat
    Club_Club -->|FoundClub| Club_Chat
    Club_Club -->|RemovedClubMember| Club_Chat
    Club_Recruitment -->|ApprovedApplication| Club_Club
    Club_Schedule -->|CancelledSchedule| Club_Feed
    Club_Schedule -->|CreatedSchedule| Club_Feed
    Club_Schedule -->|UpdatedSchedule| Club_Feed
    Club_Club -->|FoundClub| Club_Lineup
    Club_Club -->|AddedClubMember| Club_Squad
    Club_Club -->|FoundClub| Club_Squad
    Club_Club -->|RemovedClubMember| Club_Squad
```

## Event 목록

| Event | 발행 도메인 | 파일 |
|-------|-----------|------|
| ProcessedSignInEvent | auth/signin | `src/main/java/com/official/lockr/domain/auth/signin/domain/event/ProcessedSignInEvent.java` |
| ChatSseEvent | club/chat | `src/main/java/com/official/lockr/domain/club/chat/domain/event/ChatSseEvent.java` |
| CreatedChatEvent | club/chat | `src/main/java/com/official/lockr/domain/club/chat/domain/event/CreatedChatEvent.java` |
| CreatedChatRoomEvent | club/chat | `src/main/java/com/official/lockr/domain/club/chat/domain/event/CreatedChatRoomEvent.java` |
| RemovedChatterEvent | club/chat | `src/main/java/com/official/lockr/domain/club/chat/domain/event/RemovedChatterEvent.java` |
| AddedClubMemberEvent | club/club | `src/main/java/com/official/lockr/domain/club/club/domain/event/AddedClubMemberEvent.java` |
| FoundClubEvent | club/club | `src/main/java/com/official/lockr/domain/club/club/domain/event/FoundClubEvent.java` |
| RemovedClubMemberEvent | club/club | `src/main/java/com/official/lockr/domain/club/club/domain/event/RemovedClubMemberEvent.java` |
| ApprovedApplicationEvent | club/recruitment | `src/main/java/com/official/lockr/domain/club/recruitment/applications/domain/event/ApprovedApplicationEvent.java` |
| RejectedApplicationEvent | club/recruitment | `src/main/java/com/official/lockr/domain/club/recruitment/applications/domain/event/RejectedApplicationEvent.java` |
| AttendanceStatusChangedEvent | club/schedule | `src/main/java/com/official/lockr/domain/club/schedule/domain/event/AttendanceStatusChangedEvent.java` |
| CancelledScheduleEvent | club/schedule | `src/main/java/com/official/lockr/domain/club/schedule/domain/event/CancelledScheduleEvent.java` |
| CreatedScheduleEvent | club/schedule | `src/main/java/com/official/lockr/domain/club/schedule/domain/event/CreatedScheduleEvent.java` |
| UpdatedScheduleEvent | club/schedule | `src/main/java/com/official/lockr/domain/club/schedule/domain/event/UpdatedScheduleEvent.java` |
| DeletedMatchEvent | club/stats | `src/main/java/com/official/lockr/domain/club/stats/domain/event/DeletedMatchEvent.java` |
| RecordedMatchEvent | club/stats | `src/main/java/com/official/lockr/domain/club/stats/domain/event/RecordedMatchEvent.java` |
| UpdatedMatchEvent | club/stats | `src/main/java/com/official/lockr/domain/club/stats/domain/event/UpdatedMatchEvent.java` |
| CreatedNotificationEvent | notification/domain | `src/main/java/com/official/lockr/domain/notification/domain/event/CreatedNotificationEvent.java` |
| UploadedShortsEvent | shorts/domain | `src/main/java/com/official/lockr/domain/shorts/domain/event/UploadedShortsEvent.java` |
| WithdrawnUserEvent | users/domain | `src/main/java/com/official/lockr/domain/users/domain/event/WithdrawnUserEvent.java` |

## Consumer 목록

| Consumer | 도메인 | 파일 |
|----------|--------|------|
| SignInTokenConsumer | auth/signin | `src/main/java/com/official/lockr/domain/auth/signin/api/SignInTokenConsumer.java` |
| WithdrawnUserEventConsumer | auth/signin | `src/main/java/com/official/lockr/domain/auth/signin/api/WithdrawnUserEventConsumer.java` |
| ChatConsumer | club/chat | `src/main/java/com/official/lockr/domain/club/chat/api/ChatConsumer.java` |
| ChatEventListener | club/chat | `src/main/java/com/official/lockr/domain/club/chat/infrastructure/sse/ChatEventListener.java` |
| ClubEventConsumer | club/club | `src/main/java/com/official/lockr/domain/club/club/api/ClubEventConsumer.java` |
| FeedEventConsumer | club/feed | `src/main/java/com/official/lockr/domain/club/feed/api/FeedEventConsumer.java` |
| ScheduleAttendanceEventConsumer | club/schedule | `src/main/java/com/official/lockr/domain/club/schedule/infrastructure/ScheduleAttendanceEventConsumer.java` |
| ScheduleNotificationEventConsumer | club/schedule | `src/main/java/com/official/lockr/domain/club/schedule/infrastructure/ScheduleNotificationEventConsumer.java` |
| LineupConsumer | club/sport | `src/main/java/com/official/lockr/domain/club/sport/football/lineup/api/LineupConsumer.java` |
| SquadEventConsumer | club/sport | `src/main/java/com/official/lockr/domain/club/sport/football/squad/api/SquadEventConsumer.java` |
