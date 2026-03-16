package com.official.lockr.domain.club.feed.application;

import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.feed.application.command.ReportFeedCommand;
import com.official.lockr.domain.club.feed.application.usecase.ReportFeedUseCase;
import com.official.lockr.domain.club.feed.domain.Feed;
import com.official.lockr.domain.club.feed.domain.FeedClub;
import com.official.lockr.domain.club.feed.domain.FeedReportRepository;
import com.official.lockr.domain.club.feed.domain.FeedRepository;
import org.springframework.stereotype.Service;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class ReportService implements ReportFeedUseCase {

    private final FeedRepository feedRepository;
    private final FeedReportRepository feedReportRepository;
    private final FeedClub feedClub;

    public ReportService(
            final FeedRepository feedRepository,
            final FeedReportRepository feedReportRepository,
            final FeedClub feedClub
    ) {
        this.feedRepository = feedRepository;
        this.feedReportRepository = feedReportRepository;
        this.feedClub = feedClub;
    }

    @Override
    public void report(final ReportFeedCommand command) {
        verifyMember(command.userId(), command.clubId());
        final Feed feed = findFeedOrThrow(command.feedId());

        if (feed.getUserId().equals(command.userId())) {
            throw new IllegalArgumentException("자신의 피드는 신고할 수 없습니다");
        }

        feedReportRepository.save(generateUlid(), command.feedId(), command.clubId(), command.userId(), command.reason());
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