package com.official.lockr.domain.club.feed.application;

import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.feed.application.dto.*;
import com.official.lockr.domain.club.feed.application.usecase.*;
import com.official.lockr.domain.club.feed.domain.*;
import com.official.lockr.domain.club.feed.domain.comment.CommentImages;
import com.official.lockr.domain.club.feed.domain.comment.CommentVideos;
import org.springframework.stereotype.Service;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class FeedService implements CreateFeedUseCase, UpdateFeedUseCase, DeleteFeedUseCase,
        AddCommentUseCase, UpdateCommentUseCase, AddCommentHeartUseCase, RemoveCommentHeartUseCase,
        AddHeartUseCase, RemoveHeartUseCase {

    private final FeedRepository feedRepository;
    private final FeedClub feedClub;

    public FeedService(
            final FeedRepository feedRepository,
            final FeedClub feedClub
    ) {
        this.feedRepository = feedRepository;
        this.feedClub = feedClub;
    }

    @Override
    public Feed create(final CreateFeedCommand command) {
        final Member member = verifyMember(command.userId(), command.clubId());

        // NOTICE 타입은 운영진만 작성 가능
        if (command.feedType().requiresStaffPermission() && !member.isStaff()) {
            throw new IllegalArgumentException("공지사항은 운영진만 작성할 수 있습니다");
        }

        final FeedImages images = FeedImages.from(command.imageUrls(), command.userId(), generateUlid());
        final FeedVideos videos = FeedVideos.from(command.videoUrls(), command.userId(), generateUlid());

        final Feed feed = Feed.create(
                generateUlid(),
                command.clubId(),
                command.userId(),
                command.feedType(),
                command.content(),
                images,
                videos
        );

        return feedRepository.save(feed);
    }

    @Override
    public Feed update(final UpdateFeedCommand command) {
        final Feed feed = findFeedOrThrow(command.feedId());

        final FeedImages images = FeedImages.from(command.imageUrls(), command.userId(), feed.getId());
        final FeedVideos videos = FeedVideos.from(command.videoUrls(), command.userId(), feed.getId());

        feed.update(command.userId(), command.content(), images, videos);

        return feedRepository.save(feed);
    }

    @Override
    public void delete(final DeleteFeedCommand command) {
        final Member member = verifyMember(command.userId(), command.clubId());
        final Feed feed = findFeedOrThrow(command.feedId());

        feed.delete(command.userId(), member.isStaff());

        feedRepository.save(feed);
    }

    @Override
    public Feed addComment(final AddCommentCommand command) {
        verifyMember(command.userId(), findFeedOrThrow(command.feedId()).getClubId());
        final Feed feed = findFeedOrThrow(command.feedId());

        final String commentId = generateUlid();
        final CommentImages images = CommentImages.from(command.imageUrls(), command.userId(), commentId);
        final CommentVideos videos = CommentVideos.from(command.videoUrls(), command.userId(), commentId);

        feed.addComment(commentId, command.userId(), command.content(), images, videos);

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

    @Override
    public Feed addCommentHeart(final AddCommentHeartCommand command) {
        verifyMember(command.userId(), findFeedOrThrow(command.feedId()).getClubId());
        final Feed feed = findFeedOrThrow(command.feedId());

        feed.addCommentHeart(command.commentId(), command.userId(), generateUlid());

        return feedRepository.save(feed);
    }

    @Override
    public Feed removeCommentHeart(final RemoveCommentHeartCommand command) {
        final Feed feed = findFeedOrThrow(command.feedId());

        feed.removeCommentHeart(command.commentId(), command.userId());

        return feedRepository.save(feed);
    }

    @Override
    public Feed addHeart(final AddHeartCommand command) {
        verifyMember(command.userId(), findFeedOrThrow(command.feedId()).getClubId());
        final Feed feed = findFeedOrThrow(command.feedId());

        feed.addHeart(generateUlid(), command.userId());

        return feedRepository.save(feed);
    }

    /**
     * Redis 에 add/delete 정보 넣어서 5초 단위로 반영하는 로직 추가 or kafka 사용하기
     * @param command
     * @return
     */
    @Override
    public Feed removeHeart(final RemoveHeartCommand command) {
        final Feed feed = findFeedOrThrow(command.feedId());

        feed.removeHeart(command.userId());

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
