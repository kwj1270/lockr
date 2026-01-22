# 회원 탈퇴 기능 구현 계획

## 관련 파일
- Domain: `src/main/java/com/official/lockr/domain/users/domain/Users.java`
- Service: `src/main/java/com/official/lockr/domain/users/application/UsersService.java`
- UseCase: `src/main/java/com/official/lockr/domain/users/application/WithdrawUsersUsecase.java`
- Command: `src/main/java/com/official/lockr/domain/users/application/command/WithdrawUsersCommand.java`
- API: `src/main/java/com/official/lockr/domain/users/api/UsersApi.java`
- Test: `src/test/java/com/official/lockr/domain/users/`
- Context: `docs/domains/users-context.md`

---

## 요구사항 (docs/features/0001-user-features.md 참조)

- 모든 모임(클럽)에서 탈퇴하지 않으면 회원 탈퇴 불가능
- 개인 데이터 즉시 삭제 (soft delete)
- 탈퇴 회원은 로그인 불가

## 테스트 목록

### 1. 도메인 계층 (Users)

- [x] 회원은 탈퇴할 수 있다 (deletedAt 설정)
- [x] 이미 탈퇴한 회원은 다시 탈퇴할 수 없다 (예외 발생)
- [x] 회원이 탈퇴 상태인지 확인할 수 있다 (isWithdrawn)

### 2. 애플리케이션 계층 (WithdrawUsersUsecase)
**의존**: Phase 1 완료 필요

- [x] 존재하지 않는 회원은 탈퇴할 수 없다
- [x] 클럽에 가입된 회원은 탈퇴할 수 없다
- [x] 클럽에 가입되지 않은 회원은 탈퇴할 수 있다

### 3. API 계층 (UsersApi)
**의존**: Phase 1-2 완료 필요

- [x] 회원 탈퇴 API 호출 성공 시 200 응답
- [x] 클럽에 가입된 회원이 탈퇴 API 호출 시 400 응답

## 구현 순서

1. Users 도메인에 `withdraw()`, `isWithdrawn()` 메서드 추가
2. `WithdrawUsersUsecase` 인터페이스 생성
3. `WithdrawUsersCommand` 생성
4. `UsersService`에 `WithdrawUsersUsecase` 구현
5. 클럽 멤버십 조회를 위한 인터페이스 정의 (ClubMembershipChecker)
6. `UsersApi`에 탈퇴 엔드포인트 추가

---

## 완료 기준
- [ ] 표시: 미완료
- [x] 표시: 완료

"go" 명령 시 첫 번째 미완료 테스트부터 순서대로 진행
