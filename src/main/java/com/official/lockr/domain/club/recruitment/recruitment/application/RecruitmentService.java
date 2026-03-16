package com.official.lockr.domain.club.recruitment.recruitment.application;

import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.club.recruitment.recruitment.application.command.PostRecruitmentCommand;
import com.official.lockr.domain.club.recruitment.recruitment.application.command.UpdateRecruitmentCommand;
import com.official.lockr.domain.club.recruitment.recruitment.application.usecase.PostRecruitmentUseCase;
import com.official.lockr.domain.club.recruitment.recruitment.application.usecase.UpdateRecruitmentUseCase;
import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;
import com.official.lockr.domain.club.recruitment.recruitment.domain.RecruitmentRepository;
import com.official.lockr.domain.club.recruitment.recruitment.domain.vo.RecruitmentType;
import org.springframework.stereotype.Service;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class RecruitmentService implements PostRecruitmentUseCase, UpdateRecruitmentUseCase {

    private final ClubRepository clubRepository;
    private final RecruitmentRepository recruitmentRepository;

    public RecruitmentService(
            final ClubRepository clubRepository,
            final RecruitmentRepository recruitmentRepository
    ) {
        this.clubRepository = clubRepository;
        this.recruitmentRepository = recruitmentRepository;
    }

    @Override
    public Recruitment post(final PostRecruitmentCommand command) {
        final Club club = club(command.clubId(), command.userId());
        final Recruitment existedRecruitment = recruitmentRepository.findByClubId(club.getId());
        if (nonNull(existedRecruitment) && existedRecruitment.isOpen()) {
            return existedRecruitment;
        }
        final Recruitment recruitment = Recruitment.post(
                generateUlid(), club.getId(), command.isPublic(),
                command.title(), command.content(), RecruitmentType.valueOf(command.applicationType()),
                command.activityCity(), command.activityDistrict(), command.activityDays(), command.activityTime(),
                command.monthlyFee(), command.contactMethod()
        );
        return recruitmentRepository.save(recruitment);
    }

    @Override
    public Recruitment update(final UpdateRecruitmentCommand command) {
        final Club club = club(command.clubId(), command.userId());
        final Recruitment recruitment = recruitment(command.recruitmentId());
        if (!club.isEqual(recruitment.getClubId())) {
            throw new IllegalArgumentException();
        }
        recruitment.update(
                command.isPublic(), command.title(), command.content(),
                command.status(),
                RecruitmentType.valueOf(command.applicationType()),
                command.activityCity(), command.activityDistrict(),
                command.activityDays(), command.activityTime(),
                command.monthlyFee(), command.contactMethod()
        );
        return recruitmentRepository.save(recruitment);
    }

    private Club club(final String clubId, final String userId) {
        final Club club = clubRepository.findById(clubId);
        if (isNull(club) || !club.isStaff(userId)) {
            throw new IllegalArgumentException();
        }
        return club;
    }

    private Recruitment recruitment(final String recruitmentId) {
        final Recruitment recruitment = recruitmentRepository.findById(recruitmentId);
        if (isNull(recruitment)) {
            throw new IllegalArgumentException();
        }
        return recruitment;
    }
}
