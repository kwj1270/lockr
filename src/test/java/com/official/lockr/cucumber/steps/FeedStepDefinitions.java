package com.official.lockr.cucumber.steps;

import com.official.lockr.domain.club.feed.domain.*;
import com.official.lockr.domain.club.feed.domain.comment.Comment;
import com.official.lockr.domain.club.feed.domain.comment.CommentImages;
import com.official.lockr.domain.club.feed.domain.comment.CommentVideos;
import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.먼저;

import static org.assertj.core.api.Assertions.assertThat;

public class FeedStepDefinitions {

    private final SharedState sharedState = SharedState.getInstance();
    private Feed feed;
    private String lastCommentId;
    private int commentIdCounter = 0;

    @Before
    public void setUp() {
        feed = null;
        lastCommentId = null;
        commentIdCounter = 0;
    }

    // === 배경 ===

    @먼저("{string} 사용자가 {string} 클럽에서 피드를 관리한다")
    public void 사용자가_클럽에서_피드를_관리한다(String userId, String clubId) {
        // 배경 설정 - SharedState에 사용자 등록은 CommonStepDefinitions에서 처리
    }

    @그리고("{string} 사용자가 {string} 클럽의 멤버이다")
    public void 사용자가_클럽의_멤버이다(String userId, String clubId) {
        // 배경 설정
    }

    // === 피드 생성 ===

    @만약("{string}이 {string} 제목으로 {string} 내용의 일반 피드를 작성한다")
    @만약("{string}가 {string} 제목으로 {string} 내용의 일반 피드를 작성한다")
    public void 일반_피드를_작성한다(String userId, String title, String content) {
        feed = Feed.create("feed-001", "club-001", userId, FeedType.GENERAL, title, content,
                FeedImages.empty("feed-001"), FeedVideos.empty("feed-001"));
    }

    @만약("{string}이 내용 없이 피드를 작성하려고 한다")
    @만약("{string}가 내용 없이 피드를 작성하려고 한다")
    public void 내용_없이_피드를_작성하려고_한다(String userId) {
        try {
            Feed.create("feed-001", "club-001", userId, FeedType.GENERAL, "제목", null,
                    FeedImages.empty("feed-001"), FeedVideos.empty("feed-001"));
        } catch (Exception e) {
            sharedState.setCaughtException(e);
            sharedState.setLastErrorMessage(e.getMessage());
        }
    }

    @그러면("피드가 성공적으로 생성된다")
    public void 피드가_성공적으로_생성된다() {
        assertThat(feed).isNotNull();
        assertThat(feed.isDeleted()).isFalse();
    }

    @그러면("피드 제목이 {string}이다")
    public void 피드_제목이_이다(String expectedTitle) {
        assertThat(feed.getTitle()).isEqualTo(expectedTitle);
    }

    @그러면("피드 내용이 {string}이다")
    public void 피드_내용이_이다(String expectedContent) {
        assertThat(feed.getContent()).isEqualTo(expectedContent);
    }

    // === 피드 존재 (먼저) ===

    @먼저("{string}이 작성한 피드가 존재한다")
    @먼저("{string}가 작성한 피드가 존재한다")
    public void 작성한_피드가_존재한다(String userId) {
        if (feed == null) {
            feed = Feed.create("feed-001", "club-001", userId, FeedType.GENERAL,
                    "테스트 피드", "테스트 내용",
                    FeedImages.empty("feed-001"), FeedVideos.empty("feed-001"));
        }
    }

    // === 피드 수정 ===

    @만약("{string}이 피드 내용을 {string}으로 변경한다")
    @만약("{string}가 피드 내용을 {string}으로 변경한다")
    public void 피드_내용을_변경한다(String userId, String newContent) {
        feed.update(userId, feed.getTitle(), newContent,
                FeedImages.empty(feed.getId()), FeedVideos.empty(feed.getId()));
    }

    @만약("{string}이 피드를 수정하려고 한다")
    @만약("{string}가 피드를 수정하려고 한다")
    public void 피드를_수정하려고_한다(String userId) {
        try {
            feed.update(userId, "수정 제목", "수정 내용",
                    FeedImages.empty(feed.getId()), FeedVideos.empty(feed.getId()));
        } catch (Exception e) {
            sharedState.setCaughtException(e);
            sharedState.setLastErrorMessage(e.getMessage());
        }
    }

    // === 피드 삭제 ===

    @만약("{string}이 피드를 삭제한다")
    @만약("{string}가 피드를 삭제한다")
    public void 피드를_삭제한다(String userId) {
        feed.delete(userId, false);
    }

    @만약("운영진이 피드를 삭제한다")
    public void 운영진이_피드를_삭제한다() {
        feed.delete("staff-001", true);
    }

    @만약("{string}이 피드를 삭제하려고 한다")
    @만약("{string}가 피드를 삭제하려고 한다")
    public void 피드를_삭제하려고_한다(String userId) {
        try {
            feed.delete(userId, false);
        } catch (Exception e) {
            sharedState.setCaughtException(e);
            sharedState.setLastErrorMessage(e.getMessage());
        }
    }

    @그러면("피드가 삭제 상태이다")
    public void 피드가_삭제_상태이다() {
        assertThat(feed.isDeleted()).isTrue();
    }

    @그리고("피드가 삭제되어 있다")
    public void 피드가_삭제되어_있다() {
        feed.delete(feed.getUserId(), false);
    }

    // === 댓글 ===

    @만약("{string}이 {string} 댓글을 작성한다")
    @만약("{string}가 {string} 댓글을 작성한다")
    public void 댓글을_작성한다(String userId, String content) {
        lastCommentId = "comment-" + (++commentIdCounter);
        feed.addComment(lastCommentId, userId, content,
                CommentImages.empty(lastCommentId), CommentVideos.empty(lastCommentId));
    }

    @그리고("{string}이 {string} 댓글을 작성했다")
    @그리고("{string}가 {string} 댓글을 작성했다")
    public void 댓글을_작성했다(String userId, String content) {
        lastCommentId = "comment-" + (++commentIdCounter);
        feed.addComment(lastCommentId, userId, content,
                CommentImages.empty(lastCommentId), CommentVideos.empty(lastCommentId));
    }

    @만약("{string}이 댓글을 작성하려고 한다")
    @만약("{string}가 댓글을 작성하려고 한다")
    public void 댓글을_작성하려고_한다(String userId) {
        try {
            feed.addComment("comment-err", userId, "댓글",
                    CommentImages.empty("comment-err"), CommentVideos.empty("comment-err"));
        } catch (Exception e) {
            sharedState.setCaughtException(e);
            sharedState.setLastErrorMessage(e.getMessage());
        }
    }

    @만약("{string}이 댓글을 {string}로 수정한다")
    @만약("{string}가 댓글을 {string}로 수정한다")
    public void 댓글을_수정한다(String userId, String newContent) {
        feed.updateComment(lastCommentId, userId, newContent,
                CommentImages.empty(lastCommentId), CommentVideos.empty(lastCommentId));
    }

    @그러면("피드에 댓글이 {int}개 존재한다")
    public void 피드에_댓글이_개_존재한다(int count) {
        assertThat(feed.getActiveComments()).hasSize(count);
    }

    @그러면("댓글 내용이 {string}이다")
    public void 댓글_내용이_이다(String expectedContent) {
        Comment comment = feed.getComments().stream()
                .filter(c -> c.getId().equals(lastCommentId))
                .findFirst()
                .orElseThrow();
        assertThat(comment.getContent()).isEqualTo(expectedContent);
    }

    @그리고("활성 댓글 수가 {int}이다")
    public void 활성_댓글_수가_이다(int count) {
        assertThat(feed.getActiveComments()).hasSize(count);
    }

    // === 좋아요 ===

    @만약("{string}이 피드에 좋아요를 누른다")
    @만약("{string}가 피드에 좋아요를 누른다")
    public void 피드에_좋아요를_누른다(String userId) {
        feed.addHeart("heart-" + userId, userId);
    }

    @그리고("{string}이 피드에 좋아요를 눌렀다")
    @그리고("{string}가 피드에 좋아요를 눌렀다")
    public void 피드에_좋아요를_눌렀다(String userId) {
        feed.addHeart("heart-" + userId, userId);
    }

    @만약("{string}이 중복 좋아요를 누르려고 한다")
    @만약("{string}가 중복 좋아요를 누르려고 한다")
    public void 중복_좋아요를_누르려고_한다(String userId) {
        try {
            feed.addHeart("heart-dup-" + userId, userId);
        } catch (Exception e) {
            sharedState.setCaughtException(e);
            sharedState.setLastErrorMessage(e.getMessage());
        }
    }

    @만약("{string}이 좋아요를 취소한다")
    @만약("{string}가 좋아요를 취소한다")
    public void 좋아요를_취소한다(String userId) {
        feed.removeHeart(userId);
    }

    @그러면("피드의 좋아요 수가 {int}이다")
    public void 피드의_좋아요_수가_이다(int count) {
        assertThat(feed.getHeartsCount()).isEqualTo(count);
    }

    @그리고("활성 좋아요 수가 {int}이다")
    public void 활성_좋아요_수가_이다(int count) {
        assertThat(feed.getActiveHearts()).hasSize(count);
    }

    // === 댓글 좋아요 ===

    @만약("{string}이 댓글에 좋아요를 누른다")
    @만약("{string}가 댓글에 좋아요를 누른다")
    public void 댓글에_좋아요를_누른다(String userId) {
        feed.addCommentHeart(lastCommentId, userId, "cheart-" + userId);
    }

    @그러면("댓글의 좋아요 수가 {int}이다")
    public void 댓글의_좋아요_수가_이다(int count) {
        Comment comment = feed.getComments().stream()
                .filter(c -> c.getId().equals(lastCommentId))
                .findFirst()
                .orElseThrow();
        assertThat(comment.getHeartsCount()).isEqualTo(count);
    }
}
