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
- 코치/매니저 임명 (`assignCoach()`, `assignManager()`)
- Staff 여부 확인 (`isStaff()`)

#### 도메인 이벤트
- `FoundClubEvent`: 클럽 창단 시
- `AddedClubMemberEvent`: 멤버 추가 시

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
├── minParticipants: int
├── maxParticipants: int
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
- 출석 응답 (`respond()`)
- 일정 수정 (`update()`)
- 일정 취소 (`cancel()`)
- 참석/불참/미응답 집계

#### 도메인 이벤트
- `CreatedScheduleEvent`: 일정 생성 시
- `RespondedToScheduleEvent`: 출석 응답 시
- `UpdatedScheduleEvent`: 일정 수정 시
- `CancelledScheduleEvent`: 일정 취소 시

#### 비즈니스 규칙
1. 과거 시간에 일정 생성 불가
2. 취소된 일정은 수정/응답 불가
3. 초대된 멤버만 응답 가능

---

### 3. Chat (채팅)

#### Aggregate Root: ChatRoom
```
ChatRoom
├── id: String
├── clubId: String
├── name: String
└── chatters: List<Chatter>
```

#### Entity: Chat (메시지)
```
Chat
├── id: String
├── chatRoomId: String
├── senderId: String
├── content: String
├── messageType: String
└── createdAt: LocalDateTime
```

---

### 4. Feed (피드)

#### Aggregate Root: Feed
```
Feed
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
- `GENERAL`: 일반 게시물
- (추가 타입 가능)

---

### 5. Recruitment (모집)

#### Aggregate Root: Recruitment
```
Recruitment
├── id: String
├── clubId: String
├── title: String
├── content: String
├── positions: List<String>
├── deadline: LocalDateTime
└── status: String
```

#### Aggregate Root: Application
```
Application
├── id: String
├── recruitmentId: String
├── applicantId: String
├── status: String
└── createdAt: LocalDateTime
```

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

## 도메인 간 관계

```
Club ──1:N──> Member
  │
  ├──1:N──> Schedule ──1:N──> Attendance
  │
  ├──1:N──> ChatRoom ──1:N──> Chat
  │
  ├──1:N──> Feed
  │
  ├──1:N──> Recruitment ──1:N──> Application
  │
  └──1:N──> Squad ──1:N──> SquadPlayer
              │
              └──> Lineup ──1:N──> LineupSlot
```
