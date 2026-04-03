package com.official.lockr.domain.club.recruitment.applications.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ApplicationsDao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.jooq.generated.tables.ApplicationsJOOQEntity.APPLICATIONS;
import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.RecruitmentsJOOQEntity.RECRUITMENTS;

@RestController
public class ApplicationQueryApi {

    private final ApplicationsDao applicationsDao;

    public ApplicationQueryApi(final Configuration configuration) {
        this.applicationsDao = new ApplicationsDao(configuration);
    }

    @GetMapping("/api/v1/clubs/{clubId}/applications")
    public ResponseEntity<ClubApplicationsResponse> getClubApplications(
            @PathVariable final String clubId,
            @RequestParam(value = "status", required = false) final String status,
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        var query = applicationsDao.ctx()
                .select(
                        APPLICATIONS.ID,
                        APPLICATIONS.RECRUITMENT_ID,
                        RECRUITMENTS.TITLE.as("recruitmentTitle"),
                        APPLICATIONS.APPLICANT_USER_ID,
                        APPLICATIONS.APPLICATION_TYPE,
                        APPLICATIONS.STATUS,
                        APPLICATIONS.NAME,
                        APPLICATIONS.PHONE,
                        APPLICATIONS.EMAIL,
                        APPLICATIONS.GENDER,
                        APPLICATIONS.INTRODUCTION,
                        APPLICATIONS.SPORT_TYPE,
                        APPLICATIONS.PROFILE_IMAGE_URL,
                        APPLICATIONS.REJECT_REASON,
                        APPLICATIONS.CREATED_AT,
                        APPLICATIONS.PROCESSED_AT
                )
                .from(APPLICATIONS)
                .join(RECRUITMENTS).on(APPLICATIONS.RECRUITMENT_ID.eq(RECRUITMENTS.ID))
                .where(APPLICATIONS.CLUB_ID.eq(clubId))
                .and(APPLICATIONS.DELETED_AT.isNull());

        if (status != null && !status.isEmpty()) {
            query = query.and(APPLICATIONS.STATUS.eq(status));
        }

        final List<ClubApplicationResponse> applications = query
                .orderBy(APPLICATIONS.CREATED_AT.desc())
                .fetch()
                .map(record -> new ClubApplicationResponse(
                        record.get(APPLICATIONS.ID),
                        record.get(APPLICATIONS.RECRUITMENT_ID),
                        record.get(RECRUITMENTS.TITLE.as("recruitmentTitle"), String.class),
                        record.get(APPLICATIONS.APPLICANT_USER_ID),
                        record.get(APPLICATIONS.APPLICATION_TYPE),
                        record.get(APPLICATIONS.STATUS),
                        record.get(APPLICATIONS.NAME),
                        record.get(APPLICATIONS.PHONE),
                        record.get(APPLICATIONS.EMAIL),
                        record.get(APPLICATIONS.GENDER),
                        record.get(APPLICATIONS.INTRODUCTION),
                        null,
                        record.get(APPLICATIONS.SPORT_TYPE),
                        record.get(APPLICATIONS.PROFILE_IMAGE_URL),
                        record.get(APPLICATIONS.REJECT_REASON),
                        record.get(APPLICATIONS.CREATED_AT) != null ? record.get(APPLICATIONS.CREATED_AT).toString() : null,
                        record.get(APPLICATIONS.PROCESSED_AT) != null ? record.get(APPLICATIONS.PROCESSED_AT).toString() : null
                ));

        return ResponseEntity.ok(new ClubApplicationsResponse(applications));
    }

    @GetMapping("/api/v1/applications/my")
    public ResponseEntity<MyApplicationsResponse> getMyApplications(
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        final List<MyApplicationResponse> applications = applicationsDao.ctx()
                .select(
                        APPLICATIONS.ID,
                        APPLICATIONS.CLUB_ID,
                        CLUBS.NAME.as("clubName"),
                        CLUBS.SPORT_TYPE.as("clubSportType"),
                        APPLICATIONS.RECRUITMENT_ID,
                        RECRUITMENTS.TITLE.as("recruitmentTitle"),
                        APPLICATIONS.APPLICATION_TYPE,
                        APPLICATIONS.STATUS,
                        APPLICATIONS.INTRODUCTION,
                        APPLICATIONS.REJECT_REASON,
                        APPLICATIONS.CREATED_AT,
                        APPLICATIONS.PROCESSED_AT
                )
                .from(APPLICATIONS)
                .join(CLUBS).on(APPLICATIONS.CLUB_ID.eq(CLUBS.ID))
                .join(RECRUITMENTS).on(APPLICATIONS.RECRUITMENT_ID.eq(RECRUITMENTS.ID))
                .where(APPLICATIONS.APPLICANT_USER_ID.eq(signInSession.userId()))
                .and(APPLICATIONS.DELETED_AT.isNull())
                .orderBy(APPLICATIONS.CREATED_AT.desc())
                .fetch()
                .map(record -> new MyApplicationResponse(
                        record.get(APPLICATIONS.ID),
                        record.get(APPLICATIONS.CLUB_ID),
                        record.get(CLUBS.NAME.as("clubName"), String.class),
                        record.get(CLUBS.SPORT_TYPE.as("clubSportType"), String.class),
                        record.get(APPLICATIONS.RECRUITMENT_ID),
                        record.get(RECRUITMENTS.TITLE.as("recruitmentTitle"), String.class),
                        record.get(APPLICATIONS.APPLICATION_TYPE),
                        record.get(APPLICATIONS.STATUS),
                        null,
                        record.get(APPLICATIONS.INTRODUCTION),
                        record.get(APPLICATIONS.REJECT_REASON),
                        record.get(APPLICATIONS.CREATED_AT) != null ? record.get(APPLICATIONS.CREATED_AT).toString() : null,
                        record.get(APPLICATIONS.PROCESSED_AT) != null ? record.get(APPLICATIONS.PROCESSED_AT).toString() : null
                ));

        return ResponseEntity.ok(new MyApplicationsResponse(applications));
    }

    record ClubApplicationsResponse(List<ClubApplicationResponse> applications) {}

    record ClubApplicationResponse(
            String id,
            String recruitmentId,
            String recruitmentTitle,
            String applicantUserId,
            String applicationType,
            String status,
            String name,
            String phone,
            String email,
            String gender,
            String introduction,
            String position,
            String sportType,
            String profileImageUrl,
            String rejectReason,
            String createdAt,
            String processedAt
    ) {}

    record MyApplicationsResponse(List<MyApplicationResponse> applications) {}

    record MyApplicationResponse(
            String id,
            String clubId,
            String clubName,
            String sportType,
            String recruitmentId,
            String recruitmentTitle,
            String applicationType,
            String status,
            String position,
            String introduction,
            String rejectReason,
            String createdAt,
            String processedAt
    ) {}
}
