# Club — 비즈니스 규칙

> Auto-generated from codebase. Do not edit manually.
> Run: `bash .claude/scripts/generate-domain-docs.sh`

| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| removeMember | 운영진은 탈퇴할 수 없습니다. 먼저 역할을 해제해주세요. | IllegalStateException |
| delegatePresident | 회장만 회장을 위임할 수 있습니다. | IllegalArgumentException |
| delegatePresident | 대상이 클럽 멤버가 아닙니다. | IllegalArgumentException |
| changeMemberRole | 회장 또는 부회장만 역할을 변경할 수 있습니다. | IllegalArgumentException |
| changeMemberRole | 회장의 역할은 변경할 수 없습니다. | IllegalArgumentException |
| changeVisibility | 회장 또는 부회장만 공개 설정을 변경할 수 있습니다. | IllegalArgumentException |
| changeJoinMethod | 회장 또는 부회장만 가입 방식을 변경할 수 있습니다. | IllegalArgumentException |
| updateInfo | 회장 또는 부회장만 클럽 정보를 수정할 수 있습니다. | IllegalArgumentException |
