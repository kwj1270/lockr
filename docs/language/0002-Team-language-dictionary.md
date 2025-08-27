# Lockr 앱 - 도메인별 용어 사전

## 📚 1. 전체 구조

### 1.1 용어 체계
**기술 용어 (DB/API)** ↔ **사용자 대면 용어 (UI/UX)**

---

## 🏆 3. 팀 관리 도메인

### 3.1 명사 (Nouns)
| 기술 용어 | 사용자 용어 | 영문 | 설명 | 예시 |
|-----------|-------------|------|------|------|
| Team | 팀 | Team | 축구/야구 동아리 그룹 | "서울FC팀에 가입하시겠습니까?" |
| Member | 멤버 | Member | 특정 팀에 속한 회원 | "멤버 목록 보기" |
| TeamRole | 역할 | Role | 팀 내에서의 권한 역할 | "회장으로 임명되셨습니다" |
| TeamSettings | 팀 설정 | Team Settings | 팀 운영 관련 설정 | "팀 설정 변경" |
| Invitation | 초대 | Invitation | 팀 가입 초대 | "초대장이 도착했습니다" |
| InviteCode | 초대 코드 | Invite Code | 팀 가입용 6자리 코드 | "초대 코드: 123456" |
| Application | 가입 신청 | Application | 팀 가입 신청서 | "가입 신청이 도착했습니다" |

### 3.2 동사/액션 (Actions)
| 기술 용어 | 사용자 용어 | 영문 | 설명 | 예시 UI |
|-----------|-------------|------|------|--------|
| createTeam | 팀 생성 | Create Team | 새로운 팀 만들기 | "팀 만들기" 버튼 |
| joinTeam | 팀 가입 | Join Team | 기존 팀에 참여 | "가입하기" 버튼 |
| leaveTeam | 팀 탈퇴 | Leave Team | 팀에서 나가기 | "팀 탈퇴하기" |
| inviteMember | 멤버 초대 | Invite Member | 새 멤버 초대 | "멤버 초대하기" |
| kickMember | 멤버 강퇴 | Kick Member | 멤버 내보내기 | "강퇴하기" |
| appointRole | 역할 임명 | Appoint Role | 역할 부여 | "부회장으로 임명" |
| dismissRole | 역할 해임 | Dismiss Role | 역할 해제 | "역할 해제하기" |
| dissolveTeam | 팀 해산 | Dissolve Team | 팀 완전 삭제 | "팀 해산하기" |
| approveApplication | 가입 승인 | Approve | 가입 신청 승인 | "승인" 버튼 |
| rejectApplication | 가입 거절 | Reject | 가입 신청 거절 | "거절" 버튼 |

### 3.3 역할 (Roles)
| 기술 용어 | 사용자 용어 | 권한 레벨 | 설명 |
|-----------|-------------|----------|------|
| LEADER | 회장 | 5 | 최고 관리자, 모든 권한 |
| VICE_LEADER | 부회장 | 4 | 일반 관리 업무 담당 |
| TREASURER | 총무 | 3 | 회비/용품 관리 담당 |
| GAME_MANAGER | 경기팀장 | 3 | 경기/포지션 관리 담당 |
| MEMBER | 일반멤버 | 1 | 기본 참여 권한 |

### 3.4 팀 상태 (Team Status)
| 기술 용어 | 사용자 용어 | 설명 |
|-----------|-------------|------|
| ACTIVE | 활성 | 정상 운영 중인 팀 |
| INACTIVE | 비활성 | 30일 이상 활동 없는 팀 |
| DISSOLVED | 해산 | 해산된 팀 |

### 3.5 카드 시스템 (Card System)
| 기술 용어 | 사용자 용어 | 아이콘 | 제재 기간 | 설명 |
|-----------|-------------|-------|----------|------|
| YELLOW_CARD | 옐로카드 | 🟨 | 3개월 | 경미한 위반 |
| RED_CARD | 레드카드 | 🟥 | 6개월 | 옐로카드 2장 누적 |
| BLACK_CARD | 블랙카드 | ⬛ | 영구 | 레드카드 + 옐로카드 1장 |

---
