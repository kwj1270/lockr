package com.official.lockr.domain.club.recruitment.recruitment.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;
import com.official.lockr.domain.club.recruitment.recruitment.domain.vo.RecruitmentType;
import com.official.lockr.domain.club.recruitment.recruitment.domain.RecruitmentRepository;
import com.official.lockr.domain.club.recruitment.recruitment.domain.vo.RecruitmentStatus;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.RecruitmentsDao;
import org.jooq.generated.tables.pojos.RecruitmentsEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static org.jooq.generated.tables.RecruitmentsJOOQEntity.RECRUITMENTS;

@Repository
public class JOOQRecruitmentRepository implements RecruitmentRepository {

    private final RecruitmentsDao recruitmentsDao;
    private final DomainEventPublisher domainEventPublisher;
    private final ObjectMapper objectMapper;

    public JOOQRecruitmentRepository(final Configuration configuration,
                                      final DomainEventPublisher domainEventPublisher,
                                      final ObjectMapper objectMapper) {
        this.recruitmentsDao = new RecruitmentsDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Nullable
    @Override
    public Recruitment findById(final String id) {
        final RecruitmentsEntity entity = recruitmentsDao.findById(id);
        if (Objects.isNull(entity)) {
            return null;
        }
        return toDomain(entity);
    }

    @Nullable
    @Override
    public Recruitment findByClubId(final String clubId) {
        final RecruitmentsEntity entity = recruitmentsDao.ctx()
                .selectFrom(RECRUITMENTS)
                .where(RECRUITMENTS.CLUB_ID.eq(clubId))
                .and(RECRUITMENTS.DELETED_AT.isNull())
                .fetchOneInto(RecruitmentsEntity.class);
        if (Objects.isNull(entity)) {
            return null;
        }
        return toDomain(entity);
    }

    @Override
    public List<Recruitment> findAllPublicRecruitments() {
        return recruitmentsDao.ctx()
                .selectFrom(RECRUITMENTS)
                .where(RECRUITMENTS.IS_PUBLIC.isTrue())
                .and(RECRUITMENTS.STATUS.eq(RecruitmentStatus.RECRUITING.name()))
                .and(RECRUITMENTS.DELETED_AT.isNull())
                .fetchInto(RecruitmentsEntity.class)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional
    @Override
    public Recruitment save(final Recruitment recruitment) {
        upsertRecruitment(recruitment);
        recruitment.publish(domainEventPublisher);
        return recruitment;
    }

    private void upsertRecruitment(final Recruitment recruitment) {
        final String activityDaysJson = toJson(recruitment.getActivityDays());

        recruitmentsDao.ctx()
                .insertInto(RECRUITMENTS)
                .set(RECRUITMENTS.ID, recruitment.getId())
                .set(RECRUITMENTS.CLUB_ID, recruitment.getClubId())
                .set(RECRUITMENTS.STATUS, recruitment.getStatus().name())
                .set(RECRUITMENTS.IS_PUBLIC, recruitment.isPublic())
                .set(RECRUITMENTS.TITLE, recruitment.getTitle())
                .set(RECRUITMENTS.CONTENT, recruitment.getContent())
                .set(RECRUITMENTS.RECRUITMENT_TYPE, recruitment.getRecruitmentType().name())
                .set(RECRUITMENTS.ACTIVITY_REGION, recruitment.getRegion())
                .set(RECRUITMENTS.ACTIVITY_DAYS, activityDaysJson)
                .set(RECRUITMENTS.ACTIVITY_TIME, recruitment.getActivityTime())
                .set(RECRUITMENTS.MONTHLY_FEE, recruitment.getMonthlyFee())
                .set(RECRUITMENTS.CONTACT_METHOD, recruitment.getContactMethod())
                .set(RECRUITMENTS.CREATED_AT, recruitment.getCreatedAt())
                .set(RECRUITMENTS.UPDATED_AT, recruitment.getUpdatedAt())
                .set(RECRUITMENTS.DELETED_AT, recruitment.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(RECRUITMENTS.STATUS, recruitment.getStatus().name())
                .set(RECRUITMENTS.IS_PUBLIC, recruitment.isPublic())
                .set(RECRUITMENTS.TITLE, recruitment.getTitle())
                .set(RECRUITMENTS.CONTENT, recruitment.getContent())
                .set(RECRUITMENTS.RECRUITMENT_TYPE, recruitment.getRecruitmentType().name())
                .set(RECRUITMENTS.ACTIVITY_REGION, recruitment.getRegion())
                .set(RECRUITMENTS.ACTIVITY_DAYS, activityDaysJson)
                .set(RECRUITMENTS.ACTIVITY_TIME, recruitment.getActivityTime())
                .set(RECRUITMENTS.MONTHLY_FEE, recruitment.getMonthlyFee())
                .set(RECRUITMENTS.CONTACT_METHOD, recruitment.getContactMethod())
                .set(RECRUITMENTS.UPDATED_AT, recruitment.getUpdatedAt())
                .set(RECRUITMENTS.DELETED_AT, recruitment.getDeletedAt())
                .execute();
    }

    private Recruitment toDomain(final RecruitmentsEntity entity) {
        final List<String> activityDays = fromJson(entity.getActivityDays());

        return new Recruitment(
                entity.getId(),
                entity.getClubId(),
                RecruitmentStatus.valueOf(entity.getStatus()),
                entity.getIsPublic() != null ? entity.getIsPublic() : false,
                entity.getTitle(),
                entity.getContent(),
                RecruitmentType.valueOf(entity.getRecruitmentType()),
                entity.getActivityRegion(),
                activityDays,
                entity.getActivityTime(),
                entity.getMonthlyFee() != null ? entity.getMonthlyFee() : 0,
                entity.getContactMethod(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private String toJson(final List<String> activityDays) {
        try {
            return objectMapper.writeValueAsString(activityDays);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize activity days: " + activityDays, e);
        }
    }

    private List<String> fromJson(final String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize activity days JSON: " + json, e);
        }
    }
}
