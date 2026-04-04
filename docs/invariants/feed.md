# Feed — 비즈니스 규칙

> Auto-generated from codebase. Do not edit manually.
> Run: `bash .claude/scripts/generate-domain-docs.sh`

| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| create | 피드 내용은 필수입니다 | IllegalArgumentException |
| createFromSchedule | 피드 내용은 필수입니다 | IllegalArgumentException |
| update | 피드 작성자만 수정할 수 있습니다 | IllegalStateException |
| update | 삭제된 피드는 수정할 수 없습니다 | IllegalStateException |
| delete | 피드 작성자 또는 운영진만 삭제할 수 있습니다 | IllegalStateException |
| addComment | 삭제된 피드에 댓글을 작성할 수 없습니다 | IllegalStateException |
| updateComment | 삭제된 피드의 댓글은 수정할 수 없습니다 | IllegalStateException |
| addCommentHeart | 삭제된 피드의 댓글에 좋아요를 누를 수 없습니다 | IllegalStateException |
| addHeart | 삭제된 피드에 좋아요를 누를 수 없습니다 | IllegalStateException |
| addHeart | 이미 좋아요를 누르셨습니다 | IllegalStateException |
| updateFromSchedule | 삭제된 피드는 수정할 수 없습니다 | IllegalStateException |
