package com.official.lockr.domain.club.feed.application;

import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.feed.application.command.AddCommentCommand;
import com.official.lockr.domain.club.feed.application.command.UpdateCommentCommand;
import com.official.lockr.domain.club.feed.application.usecase.AddCommentUseCase;
import com.official.lockr.domain.club.feed.application.usecase.UpdateCommentUseCase;
import com.official.lockr.domain.club.feed.domain.Feed;
import com.official.lockr.domain.club.feed.domain.FeedClub;
import com.official.lockr.domain.club.feed.domain.FeedRepository;
import com.official.lockr.domain.club.feed.domain.comment.CommentImages;
import com.official.lockr.domain.club.feed.domain.comment.CommentVideos;
import org.springframework.stereotype.Service;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class CommentService implements AddCommentUseCase, UpdateCommentUseCase {

    private final FeedRepository feedRepository;
    private final FeedClub feedClub;

    public CommentService(
            final FeedRepository feedRepository,
            final FeedClub feedClub
    ) {
        this.feedRepository = feedRepository;
        this.feedClub = feedClub;
    }

    @Override
    public Feed addComment(final AddCommentCommand command) {
        verifyMember(command.userId(), findFeedOrThrow(command.feedId()).getClubId());
        final Feed feed = findFeedOrThrow(command.feedId());

        final String commentId = generateUlid();
        final CommentImages images = CommentImages.from(command.imageUrls(), command.userId(), commentId);
        final CommentVideos videos = CommentVideos.from(command.videoUrls(), command.userId(), commentId);

        feed.addComment(commentId, command.userId(), command.parentCommentId(), command.content(), images, videos);

        return feedRepository.save(feed);
    }

    @Override
    public Feed updateComment(final UpdateCommentCommand command) {
        verifyMember(command.userId(), findFeedOrThrow(command.feedId()).getClubId());
        final Feed feed = findFeedOrThrow(command.feedId());

        final CommentImages images = CommentImages.from(command.imageUrls(), command.userId(), command.commentId());
        final CommentVideos videos = CommentVideos.from(command.videoUrls(), command.userId(), command.commentId());

        feed.updateComment(command.commentId(), command.userId(), command.content(), images, videos);

        return feedRepository.save(feed);
    }

    private Feed findFeedOrThrow(final String feedId) {
        final Feed feed = feedRepository.findById(feedId);
        if (isNull(feed)) {
            throw new IllegalArgumentException("피드를 찾을 수 없습니다: " + feedId);
        }
        return feed;
    }

    private Member verifyMember(final String userId, final String clubId) {
        final Member member = feedClub.findMemberByUserIdAndClubId(userId, clubId);
        if (isNull(member)) {
            throw new IllegalArgumentException("클럽 멤버가 아닙니다");
        }
        return member;
    }
}
