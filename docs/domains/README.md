# Domain Context Overview

Lockr 프로젝트의 도메인 구조와 컨텍스트를 설명합니다.

## 도메인 구조

```
domain/
├── auth/           # 인증 Bounded Context
│   ├── admin/      # 관리자
│   ├── oidc/       # OIDC 소셜 로그인
│   ├── signin/     # 로그인/세션
│   └── signup/     # 회원가입
├── users/          # 사용자 Bounded Context
├── club/           # 클럽 Bounded Context (핵심 도메인)
│   ├── club/       # 클럽 & 멤버 관리
│   ├── chat/       # 채팅
│   ├── schedule/   # 일정 & 출석
│   ├── feed/       # 피드/게시물
│   ├── recruitment/# 모집
│   │   ├── recruitment/   # 모집 공고
│   │   └── applications/  # 지원서
│   ├── stats/      # 경기 통계 & 기록
│   └── sport/      # 종목별 기능
│       └── football/
│           ├── lineup/    # 라인업
│           └── squad/     # 스쿼드
├── home/           # 홈 화면 Bounded Context
│   └── card/       # 홈 카드
├── notification/   # 알림 Bounded Context
└── shorts/         # 숏츠 Bounded Context
```

## 각 도메인 상세

- [auth-context.md](./auth-context.md) - 인증 도메인
- [users-context.md](./users-context.md) - 사용자 도메인
- [club-context.md](./club-context.md) - 클럽 도메인 (핵심)
- [notification-context.md](./notification-context.md) - 알림 도메인
- [home-context.md](./home-context.md) - 홈 도메인
