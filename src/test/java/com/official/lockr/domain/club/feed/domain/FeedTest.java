package com.official.lockr.domain.club.feed.domain;

import com.official.lockr.domain.club.feed.domain.comment.Comment;
import com.official.lockr.domain.club.feed.domain.comment.CommentImages;
import com.official.lockr.domain.club.feed.domain.comment.CommentVideos;
import com.official.lockr.domain.club.feed.domain.entity.Heart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedTest {

    private static final String FEED_ID = "feed-001";
    private static final String CLUB_ID = "club-001";
    private static final String USER_ID = "user-001";
    private static final String OTHER_USER_ID = "user-002";

    private static Feed createFeed() {
        return Feed.create(
                FEED_ID, CLUB_ID, USER_ID, FeedType.GENERAL,
                "제목", "내용",
                FeedImages.empty(FEED_ID),
                FeedVideos.empty(FEED_ID)
        );
    }

    private static CommentImages emptyCommentImages(final String commentId) {
        return CommentImages.empty(commentId);
    }

    private static CommentVideos emptyCommentVideos(final String commentId) {
        return CommentVideos.empty(commentId);
    }

    @Nested
    @DisplayName("피드 생성")
    class CreateFeed {

        @DisplayName("피드 생성 시 필수 필드가 설정되어야 한다")
        @Test
        void shouldSetRequiredFieldsWhenCreatingFeed() {
            // when
            final Feed feed = createFeed();

            // then
            assertThat(feed.getId()).isEqualTo(FEED_ID);
            assertThat(feed.getClubId()).isEqualTo(CLUB_ID);
            assertThat(feed.getUserId()).isEqualTo(USER_ID);
            assertThat(feed.getTitle()).isEqualTo("제목");
            assertThat(feed.getContent()).isEqualTo("내용");
            assertThat(feed.getFeedType()).isEqualTo(FeedType.GENERAL);
            assertThat(feed.getComments()).isEmpty();
            assertThat(feed.getHearts()).isEmpty();
            assertThat(feed.isDeleted()).isFalse();
            assertThat(feed.getCreatedAt()).isNotNull();
            assertThat(feed.getUpdatedAt()).isNotNull();
        }

        @DisplayName("내용이 null이면 예외가 발생해야 한다")
        @Test
        void shouldThrowExceptionWhenContentIsNull() {
            assertThatThrownBy(() -> Feed.create(
                    FEED_ID, CLUB_ID, USER_ID, FeedType.GENERAL,
                    "제목", null,
                    FeedImages.empty(FEED_ID), FeedVideos.empty(FEED_ID)
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("필수");
        }

        @DisplayName("내용이 빈 문자열이면 예외가 발생해야 한다")
        @Test
        void shouldThrowExceptionWhenContentIsBlank() {
            assertThatThrownBy(() -> Feed.create(
                    FEED_ID, CLUB_ID, USER_ID, FeedType.GENERAL,
                    "제목", "   ",
                    FeedImages.empty(FEED_ID), FeedVideos.empty(FEED_ID)
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("필수");
        }

        @DisplayName("NOTICE 타입은 requiresStaffPermission이 true여야 한다")
        @Test
        void shouldRequireStaffPermissionForNoticeType() {
            assertThat(FeedType.NOTICE.requiresStaffPermission()).isTrue();
            assertThat(FeedType.GENERAL.requiresStaffPermission()).isFalse();
        }
    }

    @Nested
    @DisplayName("피드 수정")
    class UpdateFeed {

        @DisplayName("작성자가 피드를 수정할 수 있다")
        @Test
        void shouldUpdateFeedByAuthor() {
            // given
            final Feed feed = createFeed();

            // when
            feed.update(USER_ID, "새 제목", "새 내용",
                    FeedImages.empty(FEED_ID), FeedVideos.empty(FEED_ID));

            // then
            assertThat(feed.getTitle()).isEqualTo("새 제목");
            assertThat(feed.getContent()).isEqualTo("새 내용");
        }

        @DisplayName("작성자가 아닌 사용자는 수정할 수 없다")
        @Test
        void shouldThrowExceptionWhenNonAuthorUpdates() {
            // given
            final Feed feed = createFeed();

            // when & then
            assertThatThrownBy(() -> feed.update(OTHER_USER_ID, "새 제목", "새 내용",
                    FeedImages.empty(FEED_ID), FeedVideos.empty(FEED_ID)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("작성자만 수정");
        }

        @DisplayName("삭제된 피드는 수정할 수 없다")
        @Test
        void shouldThrowExceptionWhenUpdatingDeletedFeed() {
            // given
            final Feed feed = createFeed();
            feed.delete(USER_ID, false);

            // when & then
            assertThatThrownBy(() -> feed.update(USER_ID, "새 제목", "새 내용",
                    FeedImages.empty(FEED_ID), FeedVideos.empty(FEED_ID)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("삭제된 피드");
        }

        @DisplayName("수정 시 기존 이미지가 soft delete되고 새 이미지가 추가된다")
        @Test
        void shouldSoftDeleteOldImagesAndAddNewOnesWhenUpdating() {
            // given
            final Feed feed = Feed.create(
                    FEED_ID, CLUB_ID, USER_ID, FeedType.GENERAL,
                    "제목", "내용",
                    FeedImages.from(Collections.singletonList("old.jpg"), USER_ID, FEED_ID),
                    FeedVideos.empty(FEED_ID)
            );
            assertThat(feed.getImages().getUrls()).containsExactly("old.jpg");

            // when
            feed.update(USER_ID, "새 제목", "새 내용",
                    FeedImages.from(Collections.singletonList("new.jpg"), USER_ID, FEED_ID),
                    FeedVideos.empty(FEED_ID));

            // then
            assertThat(feed.getImages().getUrls()).containsExactly("new.jpg");
        }
    }

    @Nested
    @DisplayName("피드 삭제")
    class DeleteFeed {

        @DisplayName("작성자가 피드를 삭제할 수 있다")
        @Test
        void shouldDeleteFeedByAuthor() {
            // given
            final Feed feed = createFeed();

            // when
            feed.delete(USER_ID, false);

            // then
            assertThat(feed.isDeleted()).isTrue();
            assertThat(feed.getDeletedAt()).isNotNull();
        }

        @DisplayName("운영진이 다른 사용자의 피드를 삭제할 수 있다")
        @Test
        void shouldDeleteFeedByStaff() {
            // given
            final Feed feed = createFeed();

            // when
            feed.delete(OTHER_USER_ID, true);

            // then
            assertThat(feed.isDeleted()).isTrue();
        }

        @DisplayName("작성자도 아니고 운영진도 아니면 삭제할 수 없다")
        @Test
        void shouldThrowExceptionWhenNonAuthorNonStaffDeletes() {
            // given
            final Feed feed = createFeed();

            // when & then
            assertThatThrownBy(() -> feed.delete(OTHER_USER_ID, false))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("작성자 또는 운영진만 삭제");
        }

        @DisplayName("피드 삭제 시 댓글과 좋아요도 함께 삭제된다")
        @Test
        void shouldCascadeDeleteCommentsAndHearts() {
            // given
            final Feed feed = createFeed();
            feed.addComment("comment-001", USER_ID, "댓글", emptyCommentImages("comment-001"), emptyCommentVideos("comment-001"));
            feed.addHeart("heart-001", OTHER_USER_ID);

            assertThat(feed.getCommentsCount()).isEqualTo(1);
            assertThat(feed.getHeartsCount()).isEqualTo(1);

            // when
            feed.delete(USER_ID, false);

            // then
            assertThat(feed.isDeleted()).isTrue();
            assertThat(feed.getActiveComments()).isEmpty();
            assertThat(feed.getActiveHearts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("댓글")
    class CommentOperations {

        @DisplayName("피드에 댓글을 추가할 수 있다")
        @Test
        void shouldAddCommentToFeed() {
            // given
            final Feed feed = createFeed();

            // when
            final Comment comment = feed.addComment("comment-001", USER_ID, "댓글 내용",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001"));

            // then
            assertThat(comment.getId()).isEqualTo("comment-001");
            assertThat(comment.getUserId()).isEqualTo(USER_ID);
            assertThat(comment.getContent()).isEqualTo("댓글 내용");
            assertThat(feed.getComments()).hasSize(1);
        }

        @DisplayName("삭제된 피드에 댓글을 추가할 수 없다")
        @Test
        void shouldThrowExceptionWhenAddingCommentToDeletedFeed() {
            // given
            final Feed feed = createFeed();
            feed.delete(USER_ID, false);

            // when & then
            assertThatThrownBy(() -> feed.addComment("comment-001", USER_ID, "댓글",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("삭제된 피드");
        }

        @DisplayName("댓글을 수정할 수 있다")
        @Test
        void shouldUpdateComment() {
            // given
            final Feed feed = createFeed();
            feed.addComment("comment-001", USER_ID, "원래 댓글",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001"));

            // when
            feed.updateComment("comment-001", USER_ID, "수정된 댓글",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001"));

            // then
            final Comment comment = feed.getComments().get(0);
            assertThat(comment.getContent()).isEqualTo("수정된 댓글");
        }

        @DisplayName("작성자가 아닌 사용자는 댓글을 수정할 수 없다")
        @Test
        void shouldThrowExceptionWhenNonAuthorUpdatesComment() {
            // given
            final Feed feed = createFeed();
            feed.addComment("comment-001", USER_ID, "댓글",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001"));

            // when & then
            assertThatThrownBy(() -> feed.updateComment("comment-001", OTHER_USER_ID, "수정",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("작성자만 수정");
        }

        @DisplayName("삭제된 피드의 댓글은 수정할 수 없다")
        @Test
        void shouldThrowExceptionWhenUpdatingCommentOnDeletedFeed() {
            // given
            final Feed feed = createFeed();
            feed.addComment("comment-001", USER_ID, "댓글",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001"));
            feed.delete(USER_ID, false);

            // when & then
            assertThatThrownBy(() -> feed.updateComment("comment-001", USER_ID, "수정",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("삭제된 피드");
        }

        @DisplayName("존재하지 않는 댓글 수정 시 예외가 발생한다")
        @Test
        void shouldThrowExceptionWhenUpdatingNonExistentComment() {
            // given
            final Feed feed = createFeed();

            // when & then
            assertThatThrownBy(() -> feed.updateComment("non-existent", USER_ID, "수정",
                    emptyCommentImages("non-existent"), emptyCommentVideos("non-existent")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("피드 좋아요")
    class HeartOperations {

        @DisplayName("피드에 좋아요를 추가할 수 있다")
        @Test
        void shouldAddHeartToFeed() {
            // given
            final Feed feed = createFeed();

            // when
            final Heart heart = feed.addHeart("heart-001", USER_ID);

            // then
            assertThat(heart.getUserId()).isEqualTo(USER_ID);
            assertThat(feed.getHeartsCount()).isEqualTo(1);
        }

        @DisplayName("이미 좋아요를 누른 사용자는 중복 좋아요를 누를 수 없다")
        @Test
        void shouldThrowExceptionWhenDuplicateHeart() {
            // given
            final Feed feed = createFeed();
            feed.addHeart("heart-001", USER_ID);

            // when & then
            assertThatThrownBy(() -> feed.addHeart("heart-002", USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 좋아요");
        }

        @DisplayName("좋아요 취소 후 다시 좋아요를 누를 수 있다")
        @Test
        void shouldAllowReHeartAfterRemoval() {
            // given
            final Feed feed = createFeed();
            feed.addHeart("heart-001", USER_ID);
            feed.removeHeart(USER_ID);

            // when
            feed.addHeart("heart-002", USER_ID);

            // then
            assertThat(feed.getHeartsCount()).isEqualTo(1);
            assertThat(feed.getActiveHearts()).hasSize(1);
        }

        @DisplayName("삭제된 피드에 좋아요를 누를 수 없다")
        @Test
        void shouldThrowExceptionWhenAddingHeartToDeletedFeed() {
            // given
            final Feed feed = createFeed();
            feed.delete(USER_ID, false);

            // when & then
            assertThatThrownBy(() -> feed.addHeart("heart-001", OTHER_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("삭제된 피드");
        }

        @DisplayName("좋아요를 취소할 수 있다")
        @Test
        void shouldRemoveHeart() {
            // given
            final Feed feed = createFeed();
            feed.addHeart("heart-001", USER_ID);
            assertThat(feed.getHeartsCount()).isEqualTo(1);

            // when
            feed.removeHeart(USER_ID);

            // then
            assertThat(feed.getHeartsCount()).isEqualTo(0);
            assertThat(feed.getActiveHearts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("댓글 좋아요")
    class CommentHeartOperations {

        @DisplayName("댓글에 좋아요를 추가할 수 있다")
        @Test
        void shouldAddCommentHeart() {
            // given
            final Feed feed = createFeed();
            feed.addComment("comment-001", USER_ID, "댓글",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001"));

            // when
            feed.addCommentHeart("comment-001", OTHER_USER_ID, "heart-001");

            // then
            final Comment comment = feed.getComments().get(0);
            assertThat(comment.getHeartsCount()).isEqualTo(1);
        }

        @DisplayName("댓글에 중복 좋아요를 누를 수 없다")
        @Test
        void shouldThrowExceptionWhenDuplicateCommentHeart() {
            // given
            final Feed feed = createFeed();
            feed.addComment("comment-001", USER_ID, "댓글",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001"));
            feed.addCommentHeart("comment-001", OTHER_USER_ID, "heart-001");

            // when & then
            assertThatThrownBy(() -> feed.addCommentHeart("comment-001", OTHER_USER_ID, "heart-002"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 좋아요");
        }

        @DisplayName("삭제된 피드의 댓글에 좋아요를 누를 수 없다")
        @Test
        void shouldThrowExceptionWhenAddingCommentHeartToDeletedFeed() {
            // given
            final Feed feed = createFeed();
            feed.addComment("comment-001", USER_ID, "댓글",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001"));
            feed.delete(USER_ID, false);

            // when & then
            assertThatThrownBy(() -> feed.addCommentHeart("comment-001", OTHER_USER_ID, "heart-001"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("삭제된 피드");
        }

        @DisplayName("댓글 좋아요를 취소할 수 있다")
        @Test
        void shouldRemoveCommentHeart() {
            // given
            final Feed feed = createFeed();
            feed.addComment("comment-001", USER_ID, "댓글",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001"));
            feed.addCommentHeart("comment-001", OTHER_USER_ID, "heart-001");

            // when
            feed.removeCommentHeart("comment-001", OTHER_USER_ID);

            // then
            final Comment comment = feed.getComments().get(0);
            assertThat(comment.getHeartsCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("집계")
    class Counts {

        @DisplayName("활성 댓글만 카운트되어야 한다")
        @Test
        void shouldCountOnlyActiveComments() {
            // given
            final Feed feed = createFeed();
            feed.addComment("comment-001", USER_ID, "댓글1",
                    emptyCommentImages("comment-001"), emptyCommentVideos("comment-001"));
            feed.addComment("comment-002", USER_ID, "댓글2",
                    emptyCommentImages("comment-002"), emptyCommentVideos("comment-002"));

            assertThat(feed.getCommentsCount()).isEqualTo(2);

            // when - 피드 전체 삭제가 아닌 개별 댓글 삭제 시뮬레이션
            feed.getComments().get(0).delete(USER_ID);

            // then
            assertThat(feed.getCommentsCount()).isEqualTo(1);
            assertThat(feed.getActiveComments()).hasSize(1);
        }

        @DisplayName("활성 좋아요만 카운트되어야 한다")
        @Test
        void shouldCountOnlyActiveHearts() {
            // given
            final Feed feed = createFeed();
            feed.addHeart("heart-001", USER_ID);
            feed.addHeart("heart-002", OTHER_USER_ID);

            assertThat(feed.getHeartsCount()).isEqualTo(2);

            // when
            feed.removeHeart(USER_ID);

            // then
            assertThat(feed.getHeartsCount()).isEqualTo(1);
            assertThat(feed.getActiveHearts()).hasSize(1);
        }
    }
}
