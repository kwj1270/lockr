# Shorts Bounded Context

숏폼 영상 도메인. 업로드, 좋아요, 댓글, 신고, 조회수.

## Aggregate

| 타입 | 이름 | 핵심 역할 |
|------|------|----------|
| AR | Shorts | 영상 업로드, 댓글/좋아요/신고 관리, 자동 숨김 |

## Domain Events

| Event | 설명 |
|-------|------|
| UploadedShortsEvent | 숏폼 업로드 시 발행 |

## Value Objects

- **ShortsHeart**: 좋아요 (userId 중복 방지 — IllegalStateException)
- **ShortsComment**: 댓글 (소유자만 관리 가능)
- **ShortsReport**: 신고 (ModerationStatus, ReportReason enum)

## 비즈니스 규칙

| 메서드 | 규칙 | 위반 시 |
|--------|------|--------|
| create | 제목 필수, 비디오 URL 필수 | IllegalArgumentException |
| delete | 작성자 또는 운영진만 삭제 가능 | IllegalStateException |
| addHeart | 삭제된 숏츠 좋아요 불가, 중복 좋아요 불가 | IllegalStateException |
| addComment | 삭제된 숏츠 댓글 불가 | IllegalStateException |
| report | 삭제된 숏츠 신고 불가, 중복 신고 불가 | IllegalStateException |
| incrementViewCount | 삭제된 숏츠 조회수 증가 불가 | IllegalStateException |

## Gotchas

- 신고 3건 자동숨김 (AUTO_HIDE_THRESHOLD=3)
- Soft delete 시 댓글/좋아요 cascade
- restore()로 숨김 해제 가능
