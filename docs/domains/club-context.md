# Club Domain Context

클럽(동호회) 관리 핵심 Bounded Context

## 서브 도메인

### 1. Club (클럽 핵심)

#### Aggregate Root: Club
```
Club (Aggregate Root)
├── id: String (ULID)
├── foundUserId: String (창단자)
├── name: String
├── sportType: String
├── city: String
├── district: String
├── description: String
├── profileImageUrl: String
├── backgroundImageUrl: String
├── isPublic: boolean (공개 여부, 기본값 true)
├── joinMethod: String (가입 방식, 기본값 "APPROVAL_REQUIRED")
├── members: List<Member>
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime
```

#### Entity: Member
```
Member
├── id: String (ULID)
├── userId: String
├── role: MemberRole
├── clubId: String
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime
```

#### MemberRole (Enum)
- `PRESIDENT`: 회장 (Staff)
- `MANAGER`: 매니저 (Staff)
- `COACH`: 코치 (Staff)
- `BASIC`: 일반 멤버

#### 주요 기능
- 클럽 창단 (`Club.init()`)
- 멤버 추가 (`addMember()`)
- 멤버 제거 (`removeMember()`) - 운영진은 탈퇴 불가
- 코치/매니저 임명 (`assignCoach()`, `assignManger()`)
- 회장 위임 (`delegatePresident()`)
- 멤버 역할 변경 (`changeMemberRole()`) - 회장/부회장만 가능
- 공개 여부 변경 (`changeVisibility()`) - 회장/부회장만 가능
- 가입 방식 변경 (`changeJoinMethod()`) - 회장/부회장만 가능
- 클럽 정보 수정 (`updateInfo()`) - 회장/부회장만 가능
- Staff 여부 확인 (`isStaff()`)
- 회장 여부 확인 (`isPresident()`)
- 회장/부회장 여부 확인 (`isPresidency()`)

#### 도메인 이벤트
- `FoundClubEvent`: 클럽 창단 시
- `AddedClubMemberEvent`: 멤버 추가 시
- `RemovedClubMemberEvent`: 멤버 제거 시

---

### 2. Schedule (일정)

#### Aggregate Root: Schedule
```
Schedule (Aggregate Root)
├── id: String
├── clubId: String
├── title: String
├── content: String
├── location: String
├── scheduleTime: LocalDateTime
├── scheduleType: ScheduleType
├── scheduleDetailData: ScheduleDetailData (JSON)
├── attendances: List<Attendance>
├── status: ScheduleStatus
├── minParticipants: Integer (nullable)
├── deadlineDays: int
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime
```

#### ScheduleType (Enum)
- `MATCH`: 경기
- `TRAINING`: 훈련
- `SOCIAL_EVENT`: 친목 행사

#### ScheduleStatus (Enum)
- `SCHEDULED`: 예정됨
- `CANCELLED`: 취소됨

#### Entity: Attendance
```
Attendance
├── id: String
├── userId: String
├── status: AttendanceStatus
├── reason: String
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime
```

#### AttendanceStatus (Enum)
- `ATTENDING`: 참석
- `NOT_ATTENDING`: 불참
- `NO_RESPONSE`: 미응답

#### 주요 기능
- 일정 생성 (`Schedule.create()`)
- 출석 응답 (`respond()`) - 본인 직접 응답
- 관리자 출석 응답 (`adminRespond()`) - 운영진이 대리 응답
- 일정 수정 (`update()`)
- 일정 취소 (`cancel()`)
- 멤버 추가 (`addMember()`)
- 참석/불참/미응답 집계

#### 도메인 이벤트
- `CreatedScheduleEvent`: 일정 생성 시
- `AttendanceStatusChangedEvent`: 출석 상태 변경 시 (이전 상태와 다를 때만 발행)
- `UpdatedScheduleEvent`: 일정 수정 시
- `CancelledScheduleEvent`: 일정 취소 시

#### AttendanceStatusChangedEvent 필드
```
AttendanceStatusChangedEvent
├── attendanceId: String
├── scheduleId: String
├── userId: String
├── changedBy: String (변경자 userId)
├── changedByRole: String (변경자 역할, 기본값 "PLAYER")
├── previousStatus: AttendanceStatus
├── newStatus: AttendanceStatus
├── reason: String (nullable)
└── changedAt: LocalDateTime
```

#### 비즈니스 규칙
1. 과거 시간에 일정 생성 불가
2. 취소된 일정은 수정/응답 불가
3. 초대된 멤버만 응답 가능
4. 이전 상태와 동일한 경우 이벤트 미발행

---

### 3. Chat (채팅)

#### Aggregate Root: ChatRoom
```
ChatRoom (Aggregate Root)
├── id: String
├── clubId: String
├── name: String
├── chatters: List<Chatter>
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime
```

#### Entity: Chatter
```
Chatter
└── userId: String
```

#### Entity: Chat (메시지)
```
Chat (Aggregate Root)
├── id: String
├── chatRoomId: String
├── senderId: String
├── message: String
├── repliedToId: String (nullable, 답장 대상 메시지 id)
├── quotedSenderName: String (nullable, 인용된 발신자명)
├── quotedContent: String (nullable, 인용된 내용)
├── createdAt: LocalDateTime
└── deletedAt: LocalDateTime (soft delete)
```

#### Entity: PinnedMessage
```
PinnedMessage
├── id: String
├── chatRoomId: String
├── chatId: String
├── pinnedBy: String (고정한 userId)
└── createdAt: LocalDateTime
```

#### 채팅 SSE 이벤트: ChatSseEvent
```
ChatSseEvent
├── chatRoomId: String
├── clubId: String
├── type: ChatSseEventType
├── chat: Chat (nullable)
└── removedUserId: String (nullable)
```

#### ChatSseEventType (Enum)
- `ROOM_CREATED`: 채팅방 생성
- `ROOM_UPDATED`: 채팅방 정보 변경
- `NEW_MESSAGE`: 새 메시지
- `MESSAGE_UPDATED`: 메시지 수정
- `MESSAGE_DELETED`: 메시지 삭제
- `MESSAGE_PINNED`: 메시지 고정
- `MESSAGE_UNPINNED`: 메시지 고정 해제
- `CHATTER_JOINED`: 채팅 참여자 입장
- `CHATTER_LEFT`: 채팅 참여자 퇴장

#### 도메인 이벤트
- `CreatedChatRoomEvent`: 채팅방 생성 시
- `CreatedChatEvent`: 메시지 전송 시 (clubId는 EventListener에서 채움)
- `RemovedChatterEvent`: 채팅 참여자 퇴장 시

---

### 4. Feed (피드)

#### Aggregate Root: Feed
```
Feed (Aggregate Root)
├── id: String
├── clubId: String
├── authorId: String
├── content: String
├── feedType: FeedType
├── images: FeedImages
├── videos: FeedVideos
└── createdAt: LocalDateTime
```

#### FeedType (Enum)
- `GENERAL`: 일반 게시물 - 모든 멤버 작성 가능
- `NOTICE`: 공지사항 - 운영진(COACH 이상)만 작성 가능
- `SCHEDULE`: 일정 피드 - 시스템 자동 생성, 수동 수정/삭제 불가

#### 신고 기능
- `FeedReportRepository`: 피드 신고 저장 인터페이스
  - `save(id, feedId, clubId, reporterUserId, reason)`: 신고 저장

---

### 5. Recruitment (모집)

#### Aggregate Root: Recruitment
```
Recruitment (Aggregate Root)
├── id: String
├── clubId: String
├── title: String
├── content: String
├── isPublic: boolean
├── status: RecruitmentStatus
├── recruitmentType: RecruitmentType
├── activityCity: String
├── activityDistrict: String
├── activityDays: Days
├── activityTime: String
├── contactMethod: String
├── monthlyFee: int
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime
```

#### RecruitmentStatus (Enum)
- `RECRUITING`: 모집중
- `PAUSED`: 모집중단

#### RecruitmentType (Enum)
- `SIMPLE`: 약식 지원서만 받음
- `DETAILED`: 정식 지원서만 받음

#### Aggregate Root: Application
```
Application (Aggregate Root)
├── id: String
├── clubId: String
├── recruitmentId: String
├── userId: String
├── applicationFormType: ApplicationFormType
├── applicationFormData: ApplicationFormData
│   ├── name: String
│   ├── phone: String
│   ├── gender: String
│   ├── introduction: String
│   └── DetailedInfo (nullable)
│       ├── profileImageUrl: String
│       ├── email: String
│       ├── address: String
│       ├── birthDate: BirthDate
│       └── emergencyContactPhone: String
├── sportType: SportType
├── sportSpecificData: SportSpecificData
├── applicationStatus: ApplicationStatus
├── processingInfo: ProcessingInfo (nullable)
│   ├── processedByUserId: String
│   ├── processedAt: LocalDateTime
│   └── reason: String
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime
```

#### ApplicationStatus (Enum)
- `SUBMITTED`: 대기 중
- `APPROVED`: 승인됨
- `REJECTED`: 거절됨
- `CANCELED`: 철회됨

#### 도메인 이벤트
- `ApprovedApplicationEvent`: 지원서 승인 시
- `RejectedApplicationEvent`: 지원서 거절 시

---

### 6. Sport/Football (축구 전용)

#### Lineup (라인업)
```
Lineup (Aggregate Root)
├── id: String
├── clubId: String
├── scheduleId: String
├── formation: String
└── slots: LineupSlots
```

#### Squad (스쿼드)
```
Squad (Aggregate Root)
├── id: String
├── clubId: String
├── name: String
└── players: List<SquadPlayer>
```

---

### 7. Stats (통계)

#### Aggregate Root: MatchRecord
```
MatchRecord (Aggregate Root)
├── id: String
├── clubId: String
├── scheduleId: String
├── matchDate: LocalDate
├── opponentName: String
├── score: MatchScore
│   ├── ourScore: int
│   └── opponentScore: int
├── result: MatchResult (자동 계산)
├── recordedBy: String (기록자 userId)
├── season: String
├── playerPerformances: List<PlayerPerformance>
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime
```

#### MatchResult (Enum)
- `WIN`: 승리 (ourScore > opponentScore)
- `DRAW`: 무승부 (ourScore == opponentScore)
- `LOSE`: 패배 (ourScore < opponentScore)

#### Value Object: MatchScore
```
MatchScore (record)
├── ourScore: int (non-negative)
└── opponentScore: int (non-negative)
```

#### Entity: PlayerPerformance
```
PlayerPerformance
├── id: String
├── matchRecordId: String
├── clubId: String
├── userId: String
├── goals: int (non-negative)
├── assists: int (non-negative)
├── isMom: boolean (Man of the Match)
└── minutesPlayed: Integer (nullable)
```

#### 주요 기능
- 경기 기록 (`MatchRecord.create()`)
- 경기 수정 (`update()`) - 새 MatchRecord 반환 (불변)
- 경기 삭제 (`delete()`) - 새 MatchRecord 반환 (불변)
- 선수 퍼포먼스 추가 (`addPlayerPerformance()`)

#### 도메인 이벤트
- `RecordedMatchEvent`: 경기 기록 생성 시
- `UpdatedMatchEvent`: 경기 기록 수정 시
- `DeletedMatchEvent`: 경기 기록 삭제 시

---

### 8. Shorts (숏츠)

#### Aggregate Root: Shorts
```
Shorts (Aggregate Root)
├── id: String
├── clubId: String
├── userId: String (작성자)
├── title: String
├── description: String
├── videoUrl: String
├── thumbnailUrl: String
├── duration: int (영상 길이, 초)
├── viewCount: long
├── hearts: List<ShortsHeart>
├── comments: List<ShortsComment>
├── moderationStatus: ModerationStatus
├── reports: List<ShortsReport>
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime
```

#### Entity: ShortsHeart
```
ShortsHeart
├── id: String
├── shortsId: String
├── userId: String
├── createdAt: LocalDateTime
└── deletedAt: LocalDateTime (soft delete)
```

#### Entity: ShortsComment
```
ShortsComment
├── id: String
├── shortsId: String
├── userId: String
├── content: String
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime (soft delete)
```

#### Entity: ShortsReport
```
ShortsReport
├── id: String
├── shortsId: String
├── userId: String (신고자)
├── reason: ReportReason
├── detail: String
└── createdAt: LocalDateTime
```

#### ModerationStatus (Enum)
- `ACTIVE`: 정상 공개
- `HIDDEN`: 신고에 의해 숨김 (신고 3건 이상 자동 전환)

#### ReportReason (Enum)
- `INAPPROPRIATE_CONTENT`: 부적절한 콘텐츠
- `SPAM`: 스팸
- `HARASSMENT`: 괴롭힘
- `OTHER`: 기타

#### 주요 기능
- 숏츠 업로드 (`Shorts.create()`)
- 숏츠 삭제 (`delete(userId, isStaff)`) - 작성자 또는 운영진
- 좋아요 추가/제거 (`addHeart()`, `removeHeart()`)
- 댓글 추가/삭제 (`addComment()`, `deleteComment()`)
- 조회수 증가 (`incrementViewCount()`)
- 신고 (`report()`) - 3건 이상 누적 시 자동 HIDDEN
- 복원 (`restore()`) - HIDDEN → ACTIVE

#### 도메인 이벤트
- `UploadedShortsEvent`: 숏츠 업로드 시

---

## 도메인 간 관계

```
Club ──1:N──> Member
  │
  ├──1:N──> Schedule ──1:N──> Attendance
  │
  ├──1:N──> ChatRoom ──1:N──> Chat
  │              └──1:N──> PinnedMessage
  │
  ├──1:N──> Feed
  │
  ├──1:N──> Recruitment ──1:N──> Application
  │
  ├──1:N──> Squad ──1:N──> SquadPlayer
  │              │
  │              └──> Lineup ──1:N──> LineupSlot
  │
  ├──1:N──> MatchRecord ──1:N──> PlayerPerformance
  │
  └──1:N──> Shorts ──1:N──> ShortsHeart
                 ├──1:N──> ShortsComment
                 └──1:N──> ShortsReport
```
