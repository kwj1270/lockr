package com.official.lockr.domain.club.feed.application;

import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.feed.application.command.AddCommentHeartCommand;
import com.official.lockr.domain.club.feed.application.command.AddHeartCommand;
import com.official.lockr.domain.club.feed.application.command.RemoveCommentHeartCommand;
import com.official.lockr.domain.club.feed.application.command.RemoveHeartCommand;
import com.official.lockr.domain.club.feed.application.usecase.AddCommentHeartUseCase;
import com.official.lockr.domain.club.feed.application.usecase.AddHeartUseCase;
import com.official.lockr.domain.club.feed.application.usecase.RemoveCommentHeartUseCase;
import com.official.lockr.domain.club.feed.application.usecase.RemoveHeartUseCase;
import com.official.lockr.domain.club.feed.domain.Feed;
import com.official.lockr.domain.club.feed.domain.FeedClub;
import com.official.lockr.domain.club.feed.domain.FeedRepository;
import org.springframework.stereotype.Service;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class HeartService implements AddHeartUseCase, RemoveHeartUseCase,
        AddCommentHeartUseCase, RemoveCommentHeartUseCase {

    private final FeedRepository feedRepository;
    private final FeedClub feedClub;

    public HeartService(
            final FeedRepository feedRepository,
            final FeedClub feedClub
    ) {
        this.feedRepository = feedRepository;
        this.feedClub = feedClub;
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
