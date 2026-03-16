package com.official.lockr.domain.shorts.application;

import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.club.domain.MemberRole;
import com.official.lockr.domain.shorts.application.command.*;
import com.official.lockr.domain.shorts.domain.Shorts;
import com.official.lockr.domain.shorts.domain.ShortsClub;
import com.official.lockr.domain.shorts.domain.ShortsComment;
import com.official.lockr.domain.shorts.domain.ShortsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ShortsServiceTest {

    private static final String SHORTS_ID = "shorts-001";
    private static final String CLUB_ID = "club-001";
    private static final String USER_ID = "user-001";
    private static final String OTHER_USER_ID = "user-002";
    private static final String VIDEO_URL = "https://example.com/video.mp4";
    private static final String THUMBNAIL_URL = "https://example.com/thumb.jpg";

    private ShortsRepository shortsRepository;
    private ShortsClub shortsClub;
    private ShortsService shortsService;

    @BeforeEach
    void setUp() {
        shortsRepository = mock(ShortsRepository.class);
        shortsClub = mock(ShortsClub.class);
        shortsService = new ShortsService(shortsRepository, shortsClub);
    }

    private Member createMember(final String userId, final MemberRole role) {
        return new Member("member-001", userId, role, CLUB_ID, "테스트", null,
                LocalDateTime.now(), LocalDateTime.now(), null);
    }

    private Shorts createExistingShorts() {
        return Shorts.create(SHORTS_ID, CLUB_ID, USER_ID, "제목", "설명", VIDEO_URL, THUMBNAIL_URL, 30);
    }

    @Nested
    @DisplayName("숏츠 업로드")
    class UploadShorts {

        @DisplayName("클럽 멤버가 숏츠를 업로드할 수 있다")
        @Test
        void shouldUploadShortsWhenUserIsClubMember() {
            // given
            when(shortsClub.findMemberByUserIdAndClubId(USER_ID, CLUB_ID))
                    .thenReturn(createMember(USER_ID, MemberRole.BASIC));
            when(shortsRepository.save(any(Shorts.class))).thenAnswer(inv -> inv.getArgument(0));

            final UploadShortsCommand command = new UploadShortsCommand(
                    USER_ID, CLUB_ID, "숏츠 제목", "숏츠 설명", VIDEO_URL, THUMBNAIL_URL, 30
            );

            // when
            final Shorts shorts = shortsService.upload(command);

            // then
            assertThat(shorts.getClubId()).isEqualTo(CLUB_ID);
            assertThat(shorts.getUserId()).isEqualTo(USER_ID);
            assertThat(shorts.getTitle()).isEqualTo("숏츠 제목");
            assertThat(shorts.getVideoUrl()).isEqualTo(VIDEO_URL);
            verify(shortsRepository).save(any(Shorts.class));
        }

        @DisplayName("클럽 멤버가 아니면 업로드할 수 없다")
        @Test
        void shouldThrowExceptionWhenUserIsNotClubMember() {
            // given
            when(shortsClub.findMemberByUserIdAndClubId(USER_ID, CLUB_ID)).thenReturn(null);

            final UploadShortsCommand command = new UploadShortsCommand(
                    USER_ID, CLUB_ID, "숏츠 제목", "숏츠 설명", VIDEO_URL, THUMBNAIL_URL, 30
            );

            // when & then
            assertThatThrownBy(() -> shortsService.upload(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("클럽 멤버가 아닙니다");
        }
    }

    @Nested
    @DisplayName("숏츠 삭제")
    class DeleteShorts {

        @DisplayName("작성자가 숏츠를 삭제할 수 있다")
        @Test
        void shouldDeleteShortsWhenUserIsAuthor() {
            // given
            when(shortsClub.findMemberByUserIdAndClubId(USER_ID, CLUB_ID))
                    .thenReturn(createMember(USER_ID, MemberRole.BASIC));
            when(shortsRepository.findById(SHORTS_ID)).thenReturn(createExistingShorts());
            when(shortsRepository.save(any(Shorts.class))).thenAnswer(inv -> inv.getArgument(0));

            final DeleteShortsCommand command = new DeleteShortsCommand(SHORTS_ID, USER_ID, CLUB_ID);

            // when
            shortsService.delete(command);

            // then
            verify(shortsRepository).save(argThat(Shorts::isDeleted));
        }

        @DisplayName("운영진이 숏츠를 삭제할 수 있다")
        @Test
        void shouldDeleteShortsWhenUserIsStaff() {
            // given
            when(shortsClub.findMemberByUserIdAndClubId(OTHER_USER_ID, CLUB_ID))
                    .thenReturn(createMember(OTHER_USER_ID, MemberRole.MANAGER));
            when(shortsRepository.findById(SHORTS_ID)).thenReturn(createExistingShorts());
            when(shortsRepository.save(any(Shorts.class))).thenAnswer(inv -> inv.getArgument(0));

            final DeleteShortsCommand command = new DeleteShortsCommand(SHORTS_ID, OTHER_USER_ID, CLUB_ID);

            // when
            shortsService.delete(command);

            // then
            verify(shortsRepository).save(argThat(Shorts::isDeleted));
        }

        @DisplayName("숏츠가 없으면 예외가 발생한다")
        @Test
        void shouldThrowExceptionWhenShortsNotFound() {
            // given
            when(shortsClub.findMemberByUserIdAndClubId(USER_ID, CLUB_ID))
                    .thenReturn(createMember(USER_ID, MemberRole.BASIC));
            when(shortsRepository.findById(SHORTS_ID)).thenReturn(null);

            final DeleteShortsCommand command = new DeleteShortsCommand(SHORTS_ID, USER_ID, CLUB_ID);

            // when & then
            assertThatThrownBy(() -> shortsService.delete(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숏츠를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("숏츠 좋아요 토글")
    class ToggleShortsHeart {

        @DisplayName("좋아요가 없으면 추가한다")
        @Test
        void shouldAddHeartWhenNotHearted() {
            // given
            final Shorts shorts = createExistingShorts();
            when(shortsRepository.findById(SHORTS_ID)).thenReturn(shorts);
            when(shortsRepository.save(any(Shorts.class))).thenAnswer(inv -> inv.getArgument(0));

            final ToggleShortsHeartCommand command = new ToggleShortsHeartCommand(SHORTS_ID, USER_ID);

            // when
            final Shorts result = shortsService.toggle(command);

            // then
            assertThat(result.getHeartsCount()).isEqualTo(1);
            verify(shortsRepository).save(any(Shorts.class));
        }

        @DisplayName("이미 좋아요면 취소한다")
        @Test
        void shouldRemoveHeartWhenAlreadyHearted() {
            // given
            final Shorts shorts = createExistingShorts();
            shorts.addHeart("heart-001", USER_ID);
            when(shortsRepository.findById(SHORTS_ID)).thenReturn(shorts);
            when(shortsRepository.save(any(Shorts.class))).thenAnswer(inv -> inv.getArgument(0));

            final ToggleShortsHeartCommand command = new ToggleShortsHeartCommand(SHORTS_ID, USER_ID);

            // when
            final Shorts result = shortsService.toggle(command);

            // then
            assertThat(result.getHeartsCount()).isEqualTo(0);
            verify(shortsRepository).save(any(Shorts.class));
        }

        @DisplayName("숏츠가 없으면 예외가 발생한다")
        @Test
        void shouldThrowExceptionWhenShortsNotFound() {
            // given
            when(shortsRepository.findById(SHORTS_ID)).thenReturn(null);

            final ToggleShortsHeartCommand command = new ToggleShortsHeartCommand(SHORTS_ID, USER_ID);

            // when & then
            assertThatThrownBy(() -> shortsService.toggle(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숏츠를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("숏츠 댓글 추가")
    class AddShortsComment {

        @DisplayName("숏츠에 댓글을 추가할 수 있다")
        @Test
        void shouldAddCommentToShorts() {
            // given
            final Shorts shorts = createExistingShorts();
            when(shortsRepository.findById(SHORTS_ID)).thenReturn(shorts);
            when(shortsRepository.save(any(Shorts.class))).thenAnswer(inv -> inv.getArgument(0));

            final AddShortsCommentCommand command = new AddShortsCommentCommand(SHORTS_ID, USER_ID, "댓글 내용");

            // when
            final ShortsComment comment = shortsService.add(command);

            // then
            assertThat(comment.getUserId()).isEqualTo(USER_ID);
            assertThat(comment.getContent()).isEqualTo("댓글 내용");
            verify(shortsRepository).save(any(Shorts.class));
        }

        @DisplayName("숏츠가 없으면 예외가 발생한다")
        @Test
        void shouldThrowExceptionWhenShortsNotFound() {
            // given
            when(shortsRepository.findById(SHORTS_ID)).thenReturn(null);

            final AddShortsCommentCommand command = new AddShortsCommentCommand(SHORTS_ID, USER_ID, "댓글");

            // when & then
            assertThatThrownBy(() -> shortsService.add(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숏츠를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("숏츠 댓글 삭제")
    class DeleteShortsComment {

        @DisplayName("댓글 작성자가 댓글을 삭제할 수 있다")
        @Test
        void shouldDeleteCommentByAuthor() {
            // given
            final Shorts shorts = createExistingShorts();
            shorts.addComment("comment-001", USER_ID, "댓글");
            when(shortsRepository.findById(SHORTS_ID)).thenReturn(shorts);
            when(shortsRepository.save(any(Shorts.class))).thenAnswer(inv -> inv.getArgument(0));

            final DeleteShortsCommentCommand command = new DeleteShortsCommentCommand(SHORTS_ID, "comment-001", USER_ID);

            // when
            shortsService.delete(command);

            // then
            verify(shortsRepository).save(argThat(s -> s.getActiveComments().isEmpty()));
        }

        @DisplayName("댓글이 없으면 예외가 발생한다")
        @Test
        void shouldThrowExceptionWhenCommentNotFound() {
            // given
            final Shorts shorts = createExistingShorts();
            when(shortsRepository.findById(SHORTS_ID)).thenReturn(shorts);

            final DeleteShortsCommentCommand command = new DeleteShortsCommentCommand(SHORTS_ID, "non-existent", USER_ID);

            // when & then
            assertThatThrownBy(() -> shortsService.delete(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글을 찾을 수 없습니다");
        }
    }
}
