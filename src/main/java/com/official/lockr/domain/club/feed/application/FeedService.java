package com.official.lockr.domain.club.feed.application;

import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.feed.application.command.CreateFeedCommand;
import com.official.lockr.domain.club.feed.application.command.DeleteFeedCommand;
import com.official.lockr.domain.club.feed.application.command.UpdateFeedCommand;
import com.official.lockr.domain.club.feed.application.usecase.CreateFeedUseCase;
import com.official.lockr.domain.club.feed.application.usecase.DeleteFeedUseCase;
import com.official.lockr.domain.club.feed.application.usecase.UpdateFeedUseCase;
import com.official.lockr.domain.club.feed.domain.*;
import org.springframework.stereotype.Service;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class FeedService implements CreateFeedUseCase, UpdateFeedUseCase, DeleteFeedUseCase {

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
                command.title(),
                command.content(),
                images,
                videos
        );

        return feedRepository.save(feed);
    }

    @Override
    public Feed update(final UpdateFeedCommand command) {
        verifyMember(command.userId(), command.clubId());
        final Feed feed = findFeedOrThrow(command.feedId());

        if (feed.isSchedule()) {
            throw new IllegalStateException("일정 피드는 수동으로 수정할 수 없습니다");
        }

        final FeedImages images = FeedImages.from(command.imageUrls(), command.userId(), feed.getId());
        final FeedVideos videos = FeedVideos.from(command.videoUrls(), command.userId(), feed.getId());

        feed.update(command.userId(), command.title(), command.content(), images, videos);

        return feedRepository.save(feed);
    }

    @Override
    public void delete(final DeleteFeedCommand command) {
        final Member member = verifyMember(command.userId(), command.clubId());
        final Feed feed = findFeedOrThrow(command.feedId());

        if (feed.isSchedule()) {
            throw new IllegalStateException("일정 피드는 수동으로 삭제할 수 없습니다");
        }

        feed.delete(command.userId(), member.isStaff());

        feedRepository.save(feed);
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
