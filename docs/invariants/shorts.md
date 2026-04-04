# Shorts — 비즈니스 규칙

> Auto-generated from codebase. Do not edit manually.
> Run: `bash .claude/scripts/generate-domain-docs.sh`

| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| create | 제목은 필수입니다 | IllegalArgumentException |
| create | 비디오 URL은 필수입니다 | IllegalArgumentException |
| delete | 작성자 또는 운영진만 삭제할 수 있습니다 | IllegalStateException |
| addHeart | 삭제된 숏츠에 좋아요를 누를 수 없습니다 | IllegalStateException |
| addHeart | 이미 좋아요를 누르셨습니다 | IllegalStateException |
| addComment | 삭제된 숏츠에 댓글을 작성할 수 없습니다 | IllegalStateException |
| incrementViewCount | 삭제된 숏츠의 조회수를 증가시킬 수 없습니다 | IllegalStateException |
| report | 삭제된 숏츠는 신고할 수 없습니다 | IllegalStateException |
| report | 이미 신고한 숏츠입니다 | IllegalStateException |
