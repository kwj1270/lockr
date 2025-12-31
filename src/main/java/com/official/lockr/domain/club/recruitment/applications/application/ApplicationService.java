package com.official.lockr.domain.club.recruitment.applications.application;

import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.club.recruitment.applications.application.command.ApproveTryoutCommand;
import com.official.lockr.domain.club.recruitment.applications.application.command.CancelApplicationCommand;
import com.official.lockr.domain.club.recruitment.applications.application.command.RejectTryoutCommand;
import com.official.lockr.domain.club.recruitment.applications.application.command.SubmitTryoutCommand;
import com.official.lockr.domain.club.recruitment.applications.application.usecase.ApproveApplicationUseCase;
import com.official.lockr.domain.club.recruitment.applications.application.usecase.CancelApplicationUseCase;
import com.official.lockr.domain.club.recruitment.applications.application.usecase.RejectApplicationUseCase;
import com.official.lockr.domain.club.recruitment.applications.application.usecase.SubmitApplicationUseCase;
import com.official.lockr.domain.club.recruitment.applications.domain.Application;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.sport.FootballSportSpecificData;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.sport.SportType;
import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;
import com.official.lockr.domain.club.recruitment.recruitment.domain.RecruitmentRepository;
import com.official.lockr.domain.club.recruitment.applications.domain.ApplicationRepository;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.sport.SportSpecificData;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.form.ApplicationFormType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class ApplicationService implements SubmitApplicationUseCase, CancelApplicationUseCase, ApproveApplicationUseCase, RejectApplicationUseCase {

    private final ClubRepository clubRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final ApplicationRepository applicationRepository;

    public ApplicationService(
            final ClubRepository clubRepository,
            final RecruitmentRepository recruitmentRepository,
            final ApplicationRepository applicationRepository
    ) {
        this.clubRepository = clubRepository;
        this.recruitmentRepository = recruitmentRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Application submit(final SubmitTryoutCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if(isNull(club) || club.isExistedUser(command.userId())) {
            throw new IllegalArgumentException();
        }
        final Recruitment recruitment = recruitmentRepository.findById(command.recruitmentId());
        if (isNull(recruitment)) {
            throw new IllegalArgumentException();
        }
        final Application existedApplication = applicationRepository.findByRecruitmentAndUser(command.recruitmentId(), command.userId());
        if (nonNull(existedApplication) && existedApplication.isActive()) {
            return existedApplication;
        }
        final SportType sportType = SportType.valueOf(command.sportType());
        final SportSpecificData sportSpecificData = factory(sportType, command.sportSpecificData());
        final ApplicationFormType applicationFormType = ApplicationFormType.valueOf(command.applicationFormType());
        final Application application = Application.create(
                generateUlid(), club.getId(), command.recruitmentId(), command.userId(),
                applicationFormType, command.name(), command.phone(), command.gender(), command.introduction(),
                command.profileImageUrl(), command.email(), command.address(), command.birthDate(), command.emergencyContactPhone(),
                sportType, sportSpecificData
        );
        return applicationRepository.save(application);
    }

    private SportSpecificData factory(final SportType sportType, final Map<String, String> stringObjectMap) {
        return switch (sportType) {
            case FOOT_BALL -> new FootballSportSpecificData(stringObjectMap);
        };
    }

    @Override
    public Application cancel(final CancelApplicationCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if(isNull(club)) {
            throw new IllegalArgumentException();
        }
        final Application application = applicationRepository.find(command.tryoutId());
        if (isNull(application) || !application.isApplicant(command.userId()) || !application.isSameClub(command.clubId())) {
            throw new IllegalArgumentException();
        }
        application.cancel();
        return applicationRepository.save(application);
    }

    @Override
    public Application approve(final ApproveTryoutCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club) || !club.isStaff(command.processedByUserId())) {
            throw new IllegalArgumentException("Club not found: " + command);
        }
        final Application application = applicationRepository.find(command.tryoutId());
        if (isNull(application) || !application.isSameClub(command.clubId()) || !application.isActive()) {
            throw new IllegalArgumentException("Tryout not found: " + command.tryoutId());
        }
        if(application.isRejected()) {
            throw new IllegalArgumentException("Tryout not found: " + command.tryoutId());
        }
        if(application.isApproved()) {
            return application;
        }
        application.approve(command.processedByUserId());
        return applicationRepository.save(application);
    }

    @Override
    public Application reject(final RejectTryoutCommand command) {
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club) || !club.isStaff(command.processedByUserId())) {
            throw new IllegalArgumentException("Club not found: " + command);
        }
        final Application application = applicationRepository.find(command.tryoutId());
        if (isNull(application) || !application.isSameClub(command.clubId()) || !application.isActive()) {
            throw new IllegalArgumentException("Tryout not found: " + command.tryoutId());
        }
        application.reject(command.processedByUserId(), command.rejectReason());
        return applicationRepository.save(application);
    }

    private static LocalDate parseDate(final String dateString) {
        if (isNull(dateString) || dateString.isBlank()) {
            throw new IllegalArgumentException("birthDate is required");
        }
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Expected: yyyy-MM-dd", e);
        }
    }
}
