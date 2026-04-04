# Schedule — 비즈니스 규칙

> Auto-generated from codebase. Do not edit manually.
> Run: `bash .claude/scripts/generate-domain-docs.sh`

| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| addMember | User already invited:  | IllegalArgumentException |
| validateMinParticipants | minParticipants must not be negative | IllegalArgumentException |
| validateDeadlineDays | deadlineDays must not be negative | IllegalArgumentException |
| validateScheduleTime | Schedule time is required | IllegalArgumentException |
| validateScheduleTime | Schedule time must be in the future | IllegalArgumentException |
| validateNotCancelled | Cannot modify cancelled schedule | IllegalStateException |
