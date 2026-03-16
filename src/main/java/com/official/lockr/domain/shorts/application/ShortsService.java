package com.official.lockr.domain.shorts.application;

import com.official.lockr.domain.shorts.domain.ShortsMember;
import com.official.lockr.domain.shorts.application.command.*;
import com.official.lockr.domain.shorts.application.usecase.*;
import com.official.lockr.domain.shorts.domain.Shorts;
import com.official.lockr.domain.shorts.domain.ShortsClub;
import com.official.lockr.domain.shorts.domain.ShortsComment;
import com.official.lockr.domain.shorts.domain.ShortsReport;
import com.official.lockr.domain.shorts.domain.ShortsRepository;
import org.springframework.stereotype.Service;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class ShortsService implements UploadShortsUseCase, DeleteShortsUseCase,
        ToggleShortsHeartUseCase, AddShortsCommentUseCase, DeleteShortsCommentUseCase,
        ReportShortsUseCase, RestoreShortsUseCase {

    private final ShortsRepository shortsRepository;
    private final ShortsClub shortsClub;

    public ShortsService(final ShortsRepository shortsRepository, final ShortsClub shortsClub) {
        this.shortsRepository = shortsRepository;
        this.shortsClub = shortsClub;
    }

    @Override
    public Shorts upload(final UploadShortsCommand command) {
        verifyMember(command.userId(), command.clubId());

        final Shorts shorts = Shorts.create(
                generateUlid(),
                command.clubId(),
                command.userId(),
                command.title(),
                command.description(),
                command.videoUrl(),
                command.thumbnailUrl(),
                command.duration()
        );

        return shortsRepository.save(shorts);
    }

    @Override
    public void delete(final DeleteShortsCommand command) {
        final ShortsMember member = verifyMember(command.userId(), command.clubId());
        final Shorts shorts = findShortsOrThrow(command.shortsId());

        shorts.delete(command.userId(), member.isStaff());

        shortsRepository.save(shorts);
    }

    @Override
    public Shorts toggle(final ToggleShortsHeartCommand command) {
        final Shorts shorts = findShortsOrThrow(command.shortsId());

        final boolean alreadyHearted = shorts.getActiveHearts().stream()
                .anyMatch(heart -> heart.getUserId().equals(command.userId()));

        if (alreadyHearted) {
            shorts.removeHeart(command.userId());
        } else {
            shorts.addHeart(generateUlid(), command.userId());
        }

        return shortsRepository.save(shorts);
    }

    @Override
    public ShortsComment add(final AddShortsCommentCommand command) {
        final Shorts shorts = findShortsOrThrow(command.shortsId());

        final ShortsComment comment = shorts.addComment(generateUlid(), command.userId(), command.content());

        shortsRepository.save(shorts);
        return comment;
    }

    @Override
    public void delete(final DeleteShortsCommentCommand command) {
        final Shorts shorts = findShortsOrThrow(command.shortsId());

        shorts.deleteComment(command.commentId(), command.userId());

        shortsRepository.save(shorts);
    }

    @Override
    public ShortsReport report(final ReportShortsCommand command) {
        verifyMember(command.userId(), command.clubId());
        final Shorts shorts = findShortsOrThrow(command.shortsId());

        final ShortsReport report = shorts.report(generateUlid(), command.userId(), command.reason(), command.detail());

        shortsRepository.save(shorts);
        return report;
    }

    @Override
    public void restore(final RestoreShortsCommand command) {
        final ShortsMember member = verifyMember(command.userId(), command.clubId());
        if (!member.isStaff()) {
            throw new IllegalStateException("운영진만 복원할 수 있습니다");
        }
        final Shorts shorts = findShortsOrThrow(command.shortsId());

        shorts.restore();

        shortsRepository.save(shorts);
    }

    private Shorts findShortsOrThrow(final String shortsId) {
        final Shorts shorts = shortsRepository.findById(shortsId);
        if (isNull(shorts)) {
            throw new IllegalArgumentException("숏츠를 찾을 수 없습니다: " + shortsId);
        }
        return shorts;
    }

    private ShortsMember verifyMember(final String userId, final String clubId) {
        final ShortsMember member = shortsClub.findMemberByUserIdAndClubId(userId, clubId);
        if (isNull(member)) {
            throw new IllegalArgumentException("클럽 멤버가 아닙니다");
        }
        return member;
    }
}
