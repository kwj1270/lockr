package com.official.lockr.domain.club.recruitment.applications.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.club.recruitment.applications.domain.Application;
import com.official.lockr.domain.club.recruitment.applications.domain.ApplicationRepository;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.ApplicationStatus;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.ProcessingInfo;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.form.ApplicationFormData;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.form.ApplicationFormType;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.sport.FootballSportSpecificData;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.sport.SportSpecificData;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.sport.SportType;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.JSON;
import org.jooq.generated.tables.daos.ApplicationsDao;
import org.jooq.generated.tables.pojos.ApplicationsEntity;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Objects;

import static org.jooq.generated.tables.ApplicationsJOOQEntity.APPLICATIONS;

@Repository
public class JOOQApplicationRepository implements ApplicationRepository {

    private final ApplicationsDao applicationsDao;
    private final ObjectMapper objectMapper;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQApplicationRepository(final Configuration configuration, final ObjectMapper objectMapper, final DomainEventPublisher domainEventPublisher) {
        this.applicationsDao = new ApplicationsDao(configuration);
        this.objectMapper = objectMapper;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Nullable
    @Override
    public Application find(final String id) {
        final ApplicationsEntity entity = applicationsDao.findById(id);
        if (Objects.isNull(entity)) {
            return null;
        }
        return toDomain(entity);
    }

    @Nullable
    @Override
    public Application findByRecruitmentAndUser(final String recruitmentId, final String userId) {
        final ApplicationsEntity entity = applicationsDao.ctx()
                .selectFrom(APPLICATIONS)
                .where(APPLICATIONS.RECRUITMENT_ID.eq(recruitmentId))
                .and(APPLICATIONS.APPLICANT_USER_ID.eq(userId))
                .fetchOneInto(ApplicationsEntity.class);
        if (Objects.isNull(entity)) {
            return null;
        }
        return toDomain(entity);
    }

    @Override
    public Application save(final Application application) {
        upsert(application);
        application.publish(domainEventPublisher);
        return application;
    }

    private void upsert(final Application application) {
        applicationsDao.ctx()
                .insertInto(APPLICATIONS)
                .set(APPLICATIONS.ID, application.getId())
                .set(APPLICATIONS.RECRUITMENT_ID, application.getRecruitmentId())
                .set(APPLICATIONS.CLUB_ID, application.getClubId())
                .set(APPLICATIONS.APPLICANT_USER_ID, application.getUserId())
                .set(APPLICATIONS.APPLICATION_TYPE, application.getApplicationFormType().name())
                .set(APPLICATIONS.STATUS, application.getApplicationStatus().name())
                .set(APPLICATIONS.PROCESSED_BY_USER_ID, application.getProcessingInfo() != null ? application.getProcessingInfo().processedByUserId() : null)
                .set(APPLICATIONS.PROCESSED_AT, application.getProcessingInfo() != null ? application.getProcessingInfo().processedAt() : null)
                .set(APPLICATIONS.REJECT_REASON, application.getProcessingInfo() != null ? application.getProcessingInfo().reason() : null)
                .set(APPLICATIONS.NAME, application.getApplicationFormData().name())
                .set(APPLICATIONS.PHONE, application.getApplicationFormData().phone())
                .set(APPLICATIONS.EMAIL, application.getApplicationFormData().detailedInfo() != null ? application.getApplicationFormData().detailedInfo().email() : "")
                .set(APPLICATIONS.BIRTH_DATE, application.getApplicationFormData().detailedInfo() != null ? application.getApplicationFormData().detailedInfo().birthDate() : null)
                .set(APPLICATIONS.GENDER, application.getApplicationFormData().gender())
                .set(APPLICATIONS.EMERGENCY_CONTACT_PHONE, application.getApplicationFormData().detailedInfo() != null ? application.getApplicationFormData().detailedInfo().emergencyContactPhone() : null)
                .set(APPLICATIONS.PROFILE_IMAGE_URL, application.getApplicationFormData().detailedInfo() != null ? application.getApplicationFormData().detailedInfo().profileImageUrl() : null)
                .set(APPLICATIONS.ADDRESS, application.getApplicationFormData().detailedInfo() != null ? application.getApplicationFormData().detailedInfo().address() : null)
                .set(APPLICATIONS.INTRODUCTION, application.getApplicationFormData().introduction())
                .set(APPLICATIONS.SPORT_TYPE, application.getSportType().name())
                .set(APPLICATIONS.SPORT_SPECIFIC_FIELDS, serializeToJson(application.getSportSpecificData()))
                .set(APPLICATIONS.CREATED_AT, application.getCreatedAt())
                .set(APPLICATIONS.UPDATED_AT, application.getUpdatedAt())
                .set(APPLICATIONS.DELETED_AT, application.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(APPLICATIONS.STATUS, application.getApplicationStatus().name())
                .set(APPLICATIONS.PROCESSED_BY_USER_ID, application.getProcessingInfo() != null ? application.getProcessingInfo().processedByUserId() : null)
                .set(APPLICATIONS.PROCESSED_AT, application.getProcessingInfo() != null ? application.getProcessingInfo().processedAt() : null)
                .set(APPLICATIONS.REJECT_REASON, application.getProcessingInfo() != null ? application.getProcessingInfo().reason() : null)
                .set(APPLICATIONS.UPDATED_AT, application.getUpdatedAt())
                .set(APPLICATIONS.DELETED_AT, application.getDeletedAt())
                .execute();
    }

    @Nullable
    @Override
    public Application findByClubAndUser(final String clubId, final String userId) {
        final ApplicationsEntity entity = applicationsDao.ctx()
                .selectFrom(APPLICATIONS)
                .where(APPLICATIONS.CLUB_ID.eq(clubId))
                .and(APPLICATIONS.APPLICANT_USER_ID.eq(userId))
                .and(APPLICATIONS.DELETED_AT.isNull())
                .fetchOneInto(ApplicationsEntity.class);
        if (Objects.isNull(entity)) {
            return null;
        }
        return toDomain(entity);
    }

    private Application toDomain(final ApplicationsEntity entity) {
        final ProcessingInfo processingInfo = entity.getProcessedByUserId() != null
                ? new ProcessingInfo(entity.getProcessedByUserId(), entity.getProcessedAt(), entity.getRejectReason())
                : null;

        final ApplicationFormData.DetailedInfo detailedInfo = new ApplicationFormData.DetailedInfo(
                entity.getProfileImageUrl(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getBirthDate(),
                entity.getEmergencyContactPhone()
        );

        final ApplicationFormData formData = new ApplicationFormData(
                entity.getName(),
                entity.getPhone(),
                entity.getGender(),
                entity.getIntroduction(),
                detailedInfo
        );

        final SportSpecificData sportSpecificData = deserializeFromJson(
                entity.getSportType(),
                entity.getSportSpecificFields()
        );

        return new Application(
                entity.getId(),
                entity.getClubId(),
                entity.getRecruitmentId(),
                entity.getApplicantUserId(),
                ApplicationFormType.valueOf(entity.getApplicationType()),
                formData,
                SportType.valueOf(entity.getSportType()),
                sportSpecificData,
                ApplicationStatus.valueOf(entity.getStatus()),
                processingInfo,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private JSON serializeToJson(final SportSpecificData sportSpecificData) {
        if (sportSpecificData == null) {
            return null;
        }
        try {
            final String jsonString = objectMapper.writeValueAsString(sportSpecificData);
            return JSON.json(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize SportSpecificData", e);
        }
    }

    @SuppressWarnings("unchecked")
    private SportSpecificData deserializeFromJson(final String sportType, final JSON json) {
        if (json == null) {
            return null;
        }
        try {
            final String jsonString = json.data();
            final Map<String, String> map = objectMapper.readValue(jsonString, Map.class);
            return switch (SportType.valueOf(sportType)) {
                case FOOT_BALL -> new FootballSportSpecificData(map);
            };
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize SportSpecificData", e);
        }
    }
}
