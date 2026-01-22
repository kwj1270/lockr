# Users Domain Context

사용자 정보 관리 Bounded Context

## Aggregate Root

### Users
- **역할**: 사용자 기본 정보 및 추가 정보 관리
- **식별자**: ULID

```
Users (Aggregate Root)
├── id: String (ULID)
├── userAdditionalInfo: UserAdditionalInfo
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── deletedAt: LocalDateTime (soft delete)
```

### UserAdditionalInfo (Value Object)
```
UserAdditionalInfo
├── id: String
├── userId: String
├── name: String
├── birthDate: BirthDate
├── phone: String
└── gender: Gender
```

## 주요 기능

### 회원 정보 수정
- `updateAdditionalInfo(name, birthDate, phone, gender)`
- 이름, 생년월일, 전화번호, 성별 수정

### 회원 탈퇴
- `withdraw()`: deletedAt 설정 (soft delete)
- `isWithdrawn()`: 탈퇴 여부 확인
- **제약조건**: 모든 클럽에서 탈퇴 후에만 회원 탈퇴 가능

## 비즈니스 규칙
1. 이미 탈퇴한 회원은 다시 탈퇴할 수 없다
2. 클럽에 가입된 상태에서는 탈퇴할 수 없다
3. 탈퇴한 회원은 로그인 불가

## 도메인 이벤트
- (현재 정의된 이벤트 없음)

## 의존 관계
- `Auth.SignUp` ← `Users`: SignUp 시 Users 생성
- `Club.Member` → `Users`: 클럽 멤버는 userId로 Users 참조
