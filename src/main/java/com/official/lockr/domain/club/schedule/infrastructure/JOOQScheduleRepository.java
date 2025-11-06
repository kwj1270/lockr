package com.official.lockr.domain.club.schedule.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleRepository;
import com.official.lockr.domain.club.schedule.domain.ScheduleStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.attendance.Attendance;
import com.official.lockr.domain.club.schedule.domain.detail.MatchDetail;
import com.official.lockr.domain.club.schedule.domain.detail.ScheduleDetail;
import com.official.lockr.domain.club.schedule.domain.detail.SocialEventDetail;
import com.official.lockr.domain.club.schedule.domain.detail.TrainingDetail;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.AttendancesDao;
import org.jooq.generated.tables.daos.SchedulesDao;
import org.jooq.generated.tables.pojos.AttendancesEntity;
import org.jooq.generated.tables.pojos.SchedulesEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

import static org.jooq.generated.tables.AttendancesJOOQEntity.ATTENDANCES;
import static org.jooq.generated.tables.SchedulesJOOQEntity.SCHEDULES;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQScheduleRepository implements ScheduleRepository {

    private final SchedulesDao schedulesDao;
    private final AttendancesDao attendancesDao;
    private final DomainEventPublisher domainEventPublisher;
    private final ObjectMapper objectMapper;

    public JOOQScheduleRepository(
            final Configuration configuration,
            final DomainEventPublisher domainEventPublisher,
            final ObjectMapper objectMapper
    ) {
        this.schedulesDao = new SchedulesDao(configuration);
        this.attendancesDao = new AttendancesDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Nullable
    @Override
    public Schedule findById(final String id) {
        final SchedulesEntity entity = schedulesDao.ctx()
                .selectFrom(SCHEDULES)
                .where(SCHEDULES.ID.eq(id))
                .fetchOneInto(SchedulesEntity.class);

        if (entity == null) {
            return null;
        }

        return toDomain(entity);
    }

    @Override
    public List<Schedule> findAllByClubId(final String clubId) {
        final List<SchedulesEntity> entities = schedulesDao.ctx()
                .selectFrom(SCHEDULES)
                .where(SCHEDULES.CLUB_ID.eq(clubId))
                .and(SCHEDULES.DELETED_AT.isNull())
                .orderBy(SCHEDULES.SCHEDULE_TIME.desc())
                .fetchInto(SchedulesEntity.class);

        return entities.stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Schedule> findAllByClubIdAndMonth(final String clubId, final YearMonth yearMonth) {
        final List<SchedulesEntity> entities = schedulesDao.ctx()
                .selectFrom(SCHEDULES)
                .where(SCHEDULES.CLUB_ID.eq(clubId))
                .and(SCHEDULES.SCHEDULE_TIME.between(
                        yearMonth.atDay(1).atStartOfDay(),
                        yearMonth.atEndOfMonth().atTime(23, 59, 59)
                ))
                .and(SCHEDULES.DELETED_AT.isNull())
                .orderBy(SCHEDULES.SCHEDULE_TIME.asc())
                .fetchInto(SchedulesEntity.class);

        return entities.stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional
    @Override
    public Schedule save(final Schedule schedule) {
        upsertSchedule(schedule);
        syncAttendances(schedule);
        schedule.publish(domainEventPublisher);
        return schedule;
    }

    @Transactional
    @Override
    public void delete(final String id) {
        schedulesDao.ctx()
                .deleteFrom(SCHEDULES)
                .where(SCHEDULES.ID.eq(id))
                .execute();
    }

    private void upsertSchedule(final Schedule schedule) {
        final String detailType = getDetailType(schedule.getDetail());
        final String detailData;
        try {
            detailData = serializeDetail(schedule.getScheduleType(), schedule.getDetail());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        schedulesDao.ctx()
                .insertInto(SCHEDULES)
                .set(SCHEDULES.ID, schedule.getId())
                .set(SCHEDULES.CLUB_ID, schedule.getClubId())
                .set(SCHEDULES.TITLE, schedule.getTitle())
                .set(SCHEDULES.CONTENT, schedule.getContent())
                .set(SCHEDULES.LOCATION, schedule.getLocation())
                .set(SCHEDULES.SCHEDULE_TIME, schedule.getScheduleTime())
                .set(SCHEDULES.SCHEDULE_TYPE, schedule.getScheduleType().name())
                .set(SCHEDULES.DETAIL_TYPE, detailType)
                .set(SCHEDULES.DETAIL_DATA, detailData)
                .set(SCHEDULES.STATUS, schedule.getStatus().name())
                .set(SCHEDULES.CREATED_AT, schedule.getCreatedAt())
                .set(SCHEDULES.UPDATED_AT, schedule.getUpdatedAt())
                .set(SCHEDULES.DELETED_AT, schedule.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(SCHEDULES.TITLE, excluded(SCHEDULES.TITLE))
                .set(SCHEDULES.CONTENT, excluded(SCHEDULES.CONTENT))
                .set(SCHEDULES.LOCATION, excluded(SCHEDULES.LOCATION))
                .set(SCHEDULES.SCHEDULE_TIME, excluded(SCHEDULES.SCHEDULE_TIME))
                .set(SCHEDULES.DETAIL_TYPE, excluded(SCHEDULES.DETAIL_TYPE))
                .set(SCHEDULES.DETAIL_DATA, excluded(SCHEDULES.DETAIL_DATA))
                .set(SCHEDULES.STATUS, excluded(SCHEDULES.STATUS))
                .set(SCHEDULES.UPDATED_AT, excluded(SCHEDULES.UPDATED_AT))
                .set(SCHEDULES.DELETED_AT, excluded(SCHEDULES.DELETED_AT))
                .execute();
    }

    private void syncAttendances(final Schedule schedule) {
        final String scheduleId = schedule.getId();

        // 1. 현재 DB의 참석자 목록 조회
        final List<String> existingUserIds = attendancesDao.ctx()
                .select(ATTENDANCES.USER_ID)
                .from(ATTENDANCES)
                .where(ATTENDANCES.SCHEDULE_ID.eq(scheduleId))
                .fetch(ATTENDANCES.USER_ID);

        // 2. 도메인의 참석자 목록
        final List<Attendance> newAttendances = schedule.getAttendances();
        final List<String> newUserIds = newAttendances.stream()
                .map(Attendance::getUserId)
                .toList();

        // 3. 삭제할 참석자 (existingUserIds - newUserIds)
        final List<String> toRemove = existingUserIds.stream()
                .filter(userId -> !newUserIds.contains(userId))
                .toList();

        // 4. 삭제 실행
        if (!toRemove.isEmpty()) {
            attendancesDao.ctx()
                    .deleteFrom(ATTENDANCES)
                    .where(ATTENDANCES.SCHEDULE_ID.eq(scheduleId))
                    .and(ATTENDANCES.USER_ID.in(toRemove))
                    .execute();
        }

        // 5. 추가/업데이트 실행 (Upsert)
        for (Attendance attendance : newAttendances) {
            attendancesDao.ctx()
                    .insertInto(ATTENDANCES)
                    .set(ATTENDANCES.ID, attendance.getId())
                    .set(ATTENDANCES.SCHEDULE_ID, scheduleId)
                    .set(ATTENDANCES.USER_ID, attendance.getUserId())
                    .set(ATTENDANCES.STATUS, attendance.getStatus().name())
                    .set(ATTENDANCES.REASON, attendance.getReason())
                    .set(ATTENDANCES.RESPONDED_AT, attendance.getRespondedAt())
                    .set(ATTENDANCES.CREATED_AT, attendance.getCreatedAt())
                    .set(ATTENDANCES.UPDATED_AT, attendance.getUpdatedAt())
                    .onDuplicateKeyUpdate()
                    .set(ATTENDANCES.STATUS, excluded(ATTENDANCES.STATUS))
                    .set(ATTENDANCES.REASON, excluded(ATTENDANCES.REASON))
                    .set(ATTENDANCES.RESPONDED_AT, excluded(ATTENDANCES.RESPONDED_AT))
                    .set(ATTENDANCES.UPDATED_AT, excluded(ATTENDANCES.UPDATED_AT))
                    .execute();
        }
    }

    private Schedule toDomain(final SchedulesEntity entity) {
        final List<Attendance> attendances = findAttendancesByScheduleId(entity.getId());
        final ScheduleDetail detail = deserializeDetail(entity.getDetailType(), entity.getDetailData());

        return new Schedule(
                entity.getId(),
                entity.getClubId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getLocation(),
                entity.getScheduleTime(),
                ScheduleType.valueOf(entity.getScheduleType()),
                detail,
                attendances,
                ScheduleStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private List<Attendance> findAttendancesByScheduleId(final String scheduleId) {
        return attendancesDao.ctx()
                .selectFrom(ATTENDANCES)
                .where(ATTENDANCES.SCHEDULE_ID.eq(scheduleId))
                .fetchInto(AttendancesEntity.class)
                .stream()
                .map(this::toAttendanceDomain)
                .toList();
    }

    private Attendance toAttendanceDomain(final AttendancesEntity entity) {
        return new Attendance(
                entity.getId(),
                entity.getUserId(),
                com.official.lockr.domain.club.schedule.domain.attendance.AttendanceStatus.valueOf(entity.getStatus()),
                entity.getReason(),
                entity.getRespondedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String getDetailType(final ScheduleDetail detail) {
        if (detail == null) {
            return null;
        }
        return detail.getClass().getSimpleName();
    }

    private String serializeDetail(final ScheduleType scheduleType, final ScheduleDetail detail) throws JsonProcessingException {
        return switch (scheduleType) {
            case MATCH -> objectMapper.writeValueAsString((MatchDetail) detail);
            case TRAINING -> objectMapper.writeValueAsString((TrainingDetail) detail);
            case SOCIAL_EVENT -> objectMapper.writeValueAsString((SocialEventDetail) detail);
        };
    }

    private ScheduleDetail deserializeDetail(final String detailType, final String detailData) {
        if (detailType == null || detailData == null) {
            return null;
        }

        try {
            return switch (detailType) {
                case "MatchDetail" -> objectMapper.readValue(detailData, MatchDetail.class);
                case "TrainingDetail" -> objectMapper.readValue(detailData, TrainingDetail.class);
                case "SocialEventDetail" -> objectMapper.readValue(detailData, SocialEventDetail.class);
                default -> throw new IllegalArgumentException("Unknown detail type: " + detailType);
            };
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize schedule detail", e);
        }
    }
}
