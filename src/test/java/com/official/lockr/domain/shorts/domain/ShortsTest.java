package com.official.lockr.domain.shorts.domain;

import com.official.lockr.global.ddd.DomainEvent;
import com.official.lockr.global.ddd.DomainEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShortsTest {

    private static final String SHORTS_ID = "shorts-001";
    private static final String CLUB_ID = "club-001";
    private static final String USER_ID = "user-001";
    private static final String OTHER_USER_ID = "user-002";
    private static final String VIDEO_URL = "https://example.com/video.mp4";
    private static final String THUMBNAIL_URL = "https://example.com/thumb.jpg";

    private static Shorts createShorts() {
        return Shorts.create(
                SHORTS_ID, CLUB_ID, USER_ID,
                "테스트 숏츠", "숏츠 설명",
                VIDEO_URL, THUMBNAIL_URL, 30
        );
    }

    @Nested
    @DisplayName("숏츠 생성")
    class CreateShorts {

        @DisplayName("숏츠 생성 시 필수 필드가 설정되어야 한다")
        @Test
        void shouldSetRequiredFieldsWhenCreatingShorts() {
            // when
            final Shorts shorts = createShorts();

            // then
            assertThat(shorts.getId()).isEqualTo(SHORTS_ID);
            assertThat(shorts.getClubId()).isEqualTo(CLUB_ID);
            assertThat(shorts.getUserId()).isEqualTo(USER_ID);
            assertThat(shorts.getTitle()).isEqualTo("테스트 숏츠");
            assertThat(shorts.getDescription()).isEqualTo("숏츠 설명");
            assertThat(shorts.getVideoUrl()).isEqualTo(VIDEO_URL);
            assertThat(shorts.getThumbnailUrl()).isEqualTo(THUMBNAIL_URL);
            assertThat(shorts.getDuration()).isEqualTo(30);
            assertThat(shorts.getViewCount()).isEqualTo(0);
            assertThat(shorts.getComments()).isEmpty();
            assertThat(shorts.getHearts()).isEmpty();
            assertThat(shorts.isDeleted()).isFalse();
            assertThat(shorts.getCreatedAt()).isNotNull();
            assertThat(shorts.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("숏츠 생성 유효성 검증")
    class CreateShortsValidation {

        @DisplayName("videoUrl이 null이면 예외가 발생해야 한다")
        @Test
        void shouldThrowExceptionWhenVideoUrlIsNull() {
            assertThatThrownBy(() -> Shorts.create(
                    SHORTS_ID, CLUB_ID, USER_ID,
                    "제목", "설명", null, THUMBNAIL_URL, 30
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비디오 URL은 필수입니다");
        }

        @DisplayName("videoUrl이 빈 문자열이면 예외가 발생해야 한다")
        @Test
        void shouldThrowExceptionWhenVideoUrlIsBlank() {
            assertThatThrownBy(() -> Shorts.create(
                    SHORTS_ID, CLUB_ID, USER_ID,
                    "제목", "설명", "   ", THUMBNAIL_URL, 30
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비디오 URL은 필수입니다");
        }

        @DisplayName("title이 null이면 예외가 발생해야 한다")
        @Test
        void shouldThrowExceptionWhenTitleIsNull() {
            assertThatThrownBy(() -> Shorts.create(
                    SHORTS_ID, CLUB_ID, USER_ID,
                    null, "설명", VIDEO_URL, THUMBNAIL_URL, 30
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("제목은 필수입니다");
        }

        @DisplayName("title이 빈 문자열이면 예외가 발생해야 한다")
        @Test
        void shouldThrowExceptionWhenTitleIsBlank() {
            assertThatThrownBy(() -> Shorts.create(
                    SHORTS_ID, CLUB_ID, USER_ID,
                    "   ", "설명", VIDEO_URL, THUMBNAIL_URL, 30
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("제목은 필수입니다");
        }
    }

    @Nested
    @DisplayName("숏츠 생성 이벤트")
    class CreateShortsEvent {

        @DisplayName("숏츠 생성 시 UploadedShortsEvent가 발행되어야 한다")
        @Test
        void shouldPublishUploadedShortsEventWhenCreating() {
            // given
            final Shorts shorts = createShorts();
            final List<DomainEvent> capturedEvents = new ArrayList<>();
            final DomainEventPublisher publisher = capturedEvents::add;

            // when
            shorts.publish(publisher);

            // then
            assertThat(capturedEvents).hasSize(1);
            assertThat(capturedEvents.get(0)).isInstanceOf(
                    com.official.lockr.domain.shorts.domain.event.UploadedShortsEvent.class);

            final var event = (com.official.lockr.domain.shorts.domain.event.UploadedShortsEvent) capturedEvents.get(0);
            assertThat(event.shortsId()).isEqualTo(SHORTS_ID);
            assertThat(event.clubId()).isEqualTo(CLUB_ID);
            assertThat(event.userId()).isEqualTo(USER_ID);
            assertThat(event.title()).isEqualTo("테스트 숏츠");
            assertThat(event.createdAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("숏츠 삭제")
    class DeleteShorts {

        @DisplayName("작성자가 숏츠를 삭제할 수 있다")
        @Test
        void shouldDeleteShortsByAuthor() {
            // given
            final Shorts shorts = createShorts();

            // when
            shorts.delete(USER_ID, false);

            // then
            assertThat(shorts.isDeleted()).isTrue();
            assertThat(shorts.getDeletedAt()).isNotNull();
        }

        @DisplayName("운영진이 다른 사용자의 숏츠를 삭제할 수 있다")
        @Test
        void shouldDeleteShortsByStaff() {
            // given
            final Shorts shorts = createShorts();

            // when
            shorts.delete(OTHER_USER_ID, true);

            // then
            assertThat(shorts.isDeleted()).isTrue();
        }

        @DisplayName("작성자도 운영진도 아니면 삭제할 수 없다")
        @Test
        void shouldThrowExceptionWhenNonAuthorNonStaffDeletes() {
            // given
            final Shorts shorts = createShorts();

            // when & then
            assertThatThrownBy(() -> shorts.delete(OTHER_USER_ID, false))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("작성자 또는 운영진만 삭제");
        }

        @DisplayName("숏츠 삭제 시 댓글과 좋아요도 함께 소프트 삭제된다")
        @Test
        void shouldCascadeDeleteCommentsAndHeartsWhenDeletingShorts() {
            // given
            final Shorts shorts = createShorts();
            shorts.addComment("comment-001", USER_ID, "댓글");
            shorts.addHeart("heart-001", OTHER_USER_ID);

            assertThat(shorts.getCommentsCount()).isEqualTo(1);
            assertThat(shorts.getHeartsCount()).isEqualTo(1);

            // when
            shorts.delete(USER_ID, false);

            // then
            assertThat(shorts.isDeleted()).isTrue();
            assertThat(shorts.getActiveComments()).isEmpty();
            assertThat(shorts.getActiveHearts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("숏츠 좋아요")
    class HeartOperations {

        @DisplayName("숏츠에 좋아요를 추가할 수 있다")
        @Test
        void shouldAddHeartToShorts() {
            // given
            final Shorts shorts = createShorts();

            // when
            shorts.addHeart("heart-001", USER_ID);

            // then
            assertThat(shorts.getHeartsCount()).isEqualTo(1);
        }

        @DisplayName("중복 좋아요 시 예외가 발생한다")
        @Test
        void shouldThrowExceptionWhenDuplicateHeart() {
            // given
            final Shorts shorts = createShorts();
            shorts.addHeart("heart-001", USER_ID);

            // when & then
            assertThatThrownBy(() -> shorts.addHeart("heart-002", USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 좋아요");
        }

        @DisplayName("좋아요를 취소할 수 있다")
        @Test
        void shouldRemoveHeart() {
            // given
            final Shorts shorts = createShorts();
            shorts.addHeart("heart-001", USER_ID);
            assertThat(shorts.getHeartsCount()).isEqualTo(1);

            // when
            shorts.removeHeart(USER_ID);

            // then
            assertThat(shorts.getHeartsCount()).isEqualTo(0);
            assertThat(shorts.getActiveHearts()).isEmpty();
        }

        @DisplayName("좋아요 취소 후 다시 좋아요가 가능하다")
        @Test
        void shouldAllowReHeartAfterRemoval() {
            // given
            final Shorts shorts = createShorts();
            shorts.addHeart("heart-001", USER_ID);
            shorts.removeHeart(USER_ID);

            // when
            shorts.addHeart("heart-002", USER_ID);

            // then
            assertThat(shorts.getHeartsCount()).isEqualTo(1);
            assertThat(shorts.getActiveHearts()).hasSize(1);
        }

        @DisplayName("삭제된 숏츠에 좋아요를 누를 수 없다")
        @Test
        void shouldThrowExceptionWhenAddingHeartToDeletedShorts() {
            // given
            final Shorts shorts = createShorts();
            shorts.delete(USER_ID, false);

            // when & then
            assertThatThrownBy(() -> shorts.addHeart("heart-001", OTHER_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("삭제된 숏츠");
        }
    }

    @Nested
    @DisplayName("숏츠 댓글")
    class CommentOperations {

        @DisplayName("숏츠에 댓글을 추가할 수 있다")
        @Test
        void shouldAddCommentToShorts() {
            // given
            final Shorts shorts = createShorts();

            // when
            final ShortsComment comment = shorts.addComment("comment-001", USER_ID, "댓글 내용");

            // then
            assertThat(comment.getId()).isEqualTo("comment-001");
            assertThat(comment.getUserId()).isEqualTo(USER_ID);
            assertThat(comment.getContent()).isEqualTo("댓글 내용");
            assertThat(shorts.getComments()).hasSize(1);
        }

        @DisplayName("삭제된 숏츠에 댓글을 추가할 수 없다")
        @Test
        void shouldThrowExceptionWhenAddingCommentToDeletedShorts() {
            // given
            final Shorts shorts = createShorts();
            shorts.delete(USER_ID, false);

            // when & then
            assertThatThrownBy(() -> shorts.addComment("comment-001", USER_ID, "댓글"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("삭제된 숏츠");
        }

        @DisplayName("빈 댓글은 작성할 수 없다")
        @Test
        void shouldThrowExceptionWhenCommentContentIsBlank() {
            // given
            final Shorts shorts = createShorts();

            // when & then
            assertThatThrownBy(() -> shorts.addComment("comment-001", USER_ID, "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글 내용은 필수입니다");
        }

        @DisplayName("댓글 작성자가 댓글을 삭제할 수 있다")
        @Test
        void shouldDeleteCommentByAuthor() {
            // given
            final Shorts shorts = createShorts();
            shorts.addComment("comment-001", USER_ID, "댓글");

            // when
            shorts.deleteComment("comment-001", USER_ID);

            // then
            assertThat(shorts.getActiveComments()).isEmpty();
            assertThat(shorts.getCommentsCount()).isEqualTo(0);
        }

        @DisplayName("작성자가 아닌 사용자는 댓글을 삭제할 수 없다")
        @Test
        void shouldThrowExceptionWhenNonAuthorDeletesComment() {
            // given
            final Shorts shorts = createShorts();
            shorts.addComment("comment-001", USER_ID, "댓글");

            // when & then
            assertThatThrownBy(() -> shorts.deleteComment("comment-001", OTHER_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글 작성자만 삭제");
        }
    }

    @Nested
    @DisplayName("숏츠 조회수")
    class ViewCountOperations {

        @DisplayName("조회수를 증가시킬 수 있다")
        @Test
        void shouldIncrementViewCount() {
            // given
            final Shorts shorts = createShorts();
            assertThat(shorts.getViewCount()).isEqualTo(0);

            // when
            shorts.incrementViewCount();

            // then
            assertThat(shorts.getViewCount()).isEqualTo(1);
        }

        @DisplayName("삭제된 숏츠의 조회수는 증가시킬 수 없다")
        @Test
        void shouldNotIncrementViewCountForDeletedShorts() {
            // given
            final Shorts shorts = createShorts();
            shorts.delete(USER_ID, false);

            // when & then
            assertThatThrownBy(() -> shorts.incrementViewCount())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("삭제된 숏츠");
        }
    }

    @Nested
    @DisplayName("숏츠 집계")
    class Counts {

        @DisplayName("활성 좋아요만 카운트되어야 한다")
        @Test
        void shouldCountOnlyActiveHearts() {
            // given
            final Shorts shorts = createShorts();
            shorts.addHeart("heart-001", USER_ID);
            shorts.addHeart("heart-002", OTHER_USER_ID);
            assertThat(shorts.getHeartsCount()).isEqualTo(2);

            // when
            shorts.removeHeart(USER_ID);

            // then
            assertThat(shorts.getHeartsCount()).isEqualTo(1);
            assertThat(shorts.getActiveHearts()).hasSize(1);
        }

        @DisplayName("활성 댓글만 카운트되어야 한다")
        @Test
        void shouldCountOnlyActiveComments() {
            // given
            final Shorts shorts = createShorts();
            shorts.addComment("comment-001", USER_ID, "댓글1");
            shorts.addComment("comment-002", USER_ID, "댓글2");
            assertThat(shorts.getCommentsCount()).isEqualTo(2);

            // when
            shorts.deleteComment("comment-001", USER_ID);

            // then
            assertThat(shorts.getCommentsCount()).isEqualTo(1);
            assertThat(shorts.getActiveComments()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("숏츠 신고")
    class ReportOperations {

        @DisplayName("숏츠를 신고할 수 있다")
        @Test
        void shouldReportShorts() {
            // given
            final Shorts shorts = createShorts();

            // when
            final ShortsReport report = shorts.report("report-001", OTHER_USER_ID, ReportReason.SPAM, "스팸입니다");

            // then
            assertThat(report.getId()).isEqualTo("report-001");
            assertThat(report.getShortsId()).isEqualTo(SHORTS_ID);
            assertThat(report.getUserId()).isEqualTo(OTHER_USER_ID);
            assertThat(report.getReason()).isEqualTo(ReportReason.SPAM);
            assertThat(report.getDetail()).isEqualTo("스팸입니다");
            assertThat(report.getCreatedAt()).isNotNull();
            assertThat(shorts.getReportsCount()).isEqualTo(1);
            assertThat(shorts.getReports()).hasSize(1);
        }

        @DisplayName("같은 유저가 같은 숏츠를 두 번 신고하면 예외가 발생한다")
        @Test
        void shouldNotReportSameShortsAgain() {
            // given
            final Shorts shorts = createShorts();
            shorts.report("report-001", OTHER_USER_ID, ReportReason.SPAM, null);

            // when & then
            assertThatThrownBy(() -> shorts.report("report-002", OTHER_USER_ID, ReportReason.HARASSMENT, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 신고한 숏츠입니다");
        }

        @DisplayName("삭제된 숏츠에 신고하면 예외가 발생한다")
        @Test
        void shouldNotReportDeletedShorts() {
            // given
            final Shorts shorts = createShorts();
            shorts.delete(USER_ID, false);

            // when & then
            assertThatThrownBy(() -> shorts.report("report-001", OTHER_USER_ID, ReportReason.SPAM, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("삭제된 숏츠는 신고할 수 없습니다");
        }

        @DisplayName("신고 3건 이상이면 자동으로 숨김 처리된다")
        @Test
        void shouldAutoHideWhenReportsReachThreshold() {
            // given
            final Shorts shorts = createShorts();

            // when - 1건 신고
            shorts.report("report-001", "user-010", ReportReason.SPAM, null);
            assertThat(shorts.getModerationStatus()).isEqualTo(ModerationStatus.ACTIVE);
            assertThat(shorts.isHidden()).isFalse();

            // when - 2건 신고
            shorts.report("report-002", "user-011", ReportReason.INAPPROPRIATE_CONTENT, null);
            assertThat(shorts.getModerationStatus()).isEqualTo(ModerationStatus.ACTIVE);
            assertThat(shorts.isHidden()).isFalse();

            // when - 3건 신고 (임계값 도달)
            shorts.report("report-003", "user-012", ReportReason.HARASSMENT, null);

            // then
            assertThat(shorts.getModerationStatus()).isEqualTo(ModerationStatus.HIDDEN);
            assertThat(shorts.isHidden()).isTrue();
            assertThat(shorts.getReportsCount()).isEqualTo(3);
        }

        @DisplayName("신고 detail은 null이 허용된다")
        @Test
        void shouldAllowNullDetail() {
            // given
            final Shorts shorts = createShorts();

            // when
            final ShortsReport report = shorts.report("report-001", OTHER_USER_ID, ReportReason.OTHER, null);

            // then
            assertThat(report.getDetail()).isNull();
        }
    }

    @Nested
    @DisplayName("숏츠 복원")
    class RestoreOperations {

        @DisplayName("숨김 처리된 숏츠를 복원할 수 있다")
        @Test
        void shouldRestoreHiddenShorts() {
            // given
            final Shorts shorts = createShorts();
            shorts.report("report-001", "user-010", ReportReason.SPAM, null);
            shorts.report("report-002", "user-011", ReportReason.SPAM, null);
            shorts.report("report-003", "user-012", ReportReason.SPAM, null);
            assertThat(shorts.isHidden()).isTrue();

            // when
            shorts.restore();

            // then
            assertThat(shorts.getModerationStatus()).isEqualTo(ModerationStatus.ACTIVE);
            assertThat(shorts.isHidden()).isFalse();
        }

        @DisplayName("ACTIVE 상태의 숏츠도 restore 호출이 가능하다")
        @Test
        void shouldAllowRestoreOnActiveShorts() {
            // given
            final Shorts shorts = createShorts();
            assertThat(shorts.getModerationStatus()).isEqualTo(ModerationStatus.ACTIVE);

            // when
            shorts.restore();

            // then
            assertThat(shorts.getModerationStatus()).isEqualTo(ModerationStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("숏츠 ModerationStatus")
    class ModerationStatusTests {

        @DisplayName("숏츠 생성 시 moderationStatus는 ACTIVE이다")
        @Test
        void shouldCreateShortsWithActiveStatus() {
            // when
            final Shorts shorts = createShorts();

            // then
            assertThat(shorts.getModerationStatus()).isEqualTo(ModerationStatus.ACTIVE);
            assertThat(shorts.isHidden()).isFalse();
        }

        @DisplayName("HIDDEN 상태에서 isHidden은 true를 반환한다")
        @Test
        void shouldReturnTrueWhenHidden() {
            // given
            final Shorts shorts = createShorts();
            shorts.report("report-001", "user-010", ReportReason.SPAM, null);
            shorts.report("report-002", "user-011", ReportReason.SPAM, null);
            shorts.report("report-003", "user-012", ReportReason.SPAM, null);

            // then
            assertThat(shorts.isHidden()).isTrue();
        }

        @DisplayName("ACTIVE 상태에서 isHidden은 false를 반환한다")
        @Test
        void shouldReturnFalseWhenActive() {
            // given
            final Shorts shorts = createShorts();

            // then
            assertThat(shorts.isHidden()).isFalse();
        }

        @DisplayName("생성 시 reports는 빈 리스트이다")
        @Test
        void shouldCreateShortsWithEmptyReports() {
            // when
            final Shorts shorts = createShorts();

            // then
            assertThat(shorts.getReports()).isEmpty();
            assertThat(shorts.getReportsCount()).isEqualTo(0);
            assertThat(shorts.getActiveReports()).isEmpty();
        }
    }
}
