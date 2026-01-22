# 관리자 로그인 기능 테스트 계획

## 관련 파일
- Domain: `src/main/java/com/official/lockr/domain/auth/admin/domain/Admin.java`
- Service: `src/main/java/com/official/lockr/domain/auth/admin/application/AdminService.java`
- API: `src/main/java/com/official/lockr/domain/auth/admin/api/AdminApi.java`
- Test: `src/test/java/com/official/lockr/domain/auth/admin/`
- Context: `docs/domains/auth-context.md`

---

## 현재 상태 분석
- `AdminService.register()` 가 등록/로그인을 동시에 처리
- 비밀번호가 평문으로 비교됨
- 비밀번호 불일치 시 `IllegalArgumentException` 발생

## 테스트 목록

### Phase 1: Admin 도메인 단위 테스트

- [x] `Admin.init()` 으로 생성 시 기본 role은 "BASIC"이다
- [x] `Admin.init()` 으로 생성 시 userId가 설정된다 (시그니처 변경됨: id, userId, password)
- [x] `Admin.hasNotUserId()` 는 userId가 null일 때 true를 반환한다
- [x] `Admin.hasNotUserId()` 는 userId가 있을 때 false를 반환한다
- [x] 동일한 id를 가진 Admin은 equals()가 true다

### Phase 2: 비밀번호 검증 로직 (Admin 도메인)
**의존**: Phase 1 완료 필요

- [x] `Admin.matchPassword(rawPassword)` 는 비밀번호가 일치하면 true를 반환한다
- [x] `Admin.matchPassword(rawPassword)` 는 비밀번호가 불일치하면 false를 반환한다

### Phase 3: AdminService 로그인 테스트
**의존**: Phase 1-2 완료 필요

- [x] 존재하는 관리자가 올바른 비밀번호로 로그인하면 Admin을 반환한다
- [x] 존재하는 관리자가 틀린 비밀번호로 로그인하면 예외가 발생한다
- [x] 존재하지 않는 id로 로그인하면 새 Admin이 생성된다
- [x] 새 Admin 생성 시 SignUpRepository를 통해 userId가 할당된다

### Phase 4: 비밀번호 암호화 (선택)
**의존**: Phase 1-3 완료 필요

- [x] 비밀번호는 암호화되어 저장된다
- [x] 로그인 시 평문 비밀번호와 암호화된 비밀번호를 비교한다

### Phase 5: API 통합 테스트
**의존**: Phase 1-4 완료 필요

- [x] POST /api/v1/auth/sign_in/admin 성공 시 200과 Admin 정보를 반환한다
- [x] POST /api/v1/auth/sign_in/admin 성공 시 세션 쿠키가 설정되고 SignIn 레코드가 생성된다
- [x] POST /api/v1/auth/sign_in/admin 비밀번호 불일치 시 401을 반환한다

---

## 완료 기준
- [ ] 표시: 미완료
- [x] 표시: 완료

"go" 명령 시 첫 번째 미완료 테스트부터 순서대로 진행
