# FeePolicy — 비즈니스 규칙

> Auto-generated from codebase. Do not edit manually.
> Run: `bash .claude/scripts/generate-domain-docs.sh`

| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| validateAmount | 회비 금액은 0원 이상이어야 합니다. | IllegalArgumentException |
| validateDueDay | 납부 기한은 1~28일 범위여야 합니다. | IllegalArgumentException |
