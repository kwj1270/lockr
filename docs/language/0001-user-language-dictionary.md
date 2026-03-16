# Lockr 앱 - 도메인별 용어 사전

## 👤📚 1. 회원 관리 도메인

### 1.1 인증

| 기술 용어               | 사용자 용어 | 영문                   | 설명       | 예시 UI       |
|---------------------|--------|----------------------|----------|-------------|
| signUp              | 회원가입   | Sign Up              | 앱에 최초 가입 | "회원가입하기" 버튼 |
| signIn              | 로그인    | Sign In              | 앱에 로그인   | "로그인" 버튼    |
| signOut             | 로그아웃   | Sign Out             | 앱에서 로그아웃 | "로그아웃" 메뉴   |
| Authorization       | 인증     | Authorization        | 인증       |             |
| BasicAuthorization  | 일반 인증  | Basic Authorization  | 일반 인증    |             |
| SmsAuthorization    | SMS 인증 | Sms Authorization    | SMS 인증   |             |
| SocialAuthorization | 소셜 인증  | Social Authorization | 구글/애플 인증 | "구글로 시작하기"  |

### 1.2 회원

| 기술 용어        | 사용자 용어 | 영문      | 설명            | 예시              |
|--------------|--------|---------|---------------|-----------------|
| Users        | 회원     | User    | 앱에 가입한 개별 사용자 | "홍길동님 환영합니다"    |
| UserProfile  | 회원 프로필 | Profile | 회원의 개인 정보     | "프로필을 수정하세요"    |
| Name         | 회원 이름  | Profile | 회원의 개인 정보     | "홍길동"           |
| PhoneNumber  | 핸드폰 번호 | Profile | 회원의 개인 정보     | "010-2046-1270" |
| Gender       | 성별     | Profile | 회원의 개인 정보     | "남/여"           |
| ProfileImage | 대표 이미지 | Profile | 회원의 개인 정보     | "프로필을 수정하세요"    |
| UserStatus   | 회원 상태  | Status  | 활성/비활성/정지 상태  | "계정이 일시 정지됨"    |

### 1.3 상태값 (Status)

| 기술 용어     | 사용자 용어 | 설명              |
|-----------|--------|-----------------|
| ACTIVE    | 활성     | 정상적으로 사용 가능한 상태 |
| INACTIVE  | 비활성    | 임시적으로 사용 중단     |
| SUSPENDED | 정지     | 관리자에 의해 사용 제한   |
| DELETED   | 탈퇴     | 계정이 삭제된 상태      |
