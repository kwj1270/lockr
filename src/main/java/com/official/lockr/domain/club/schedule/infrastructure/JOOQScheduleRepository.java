package com.official.lockr.domain.club.schedule.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleRepository;
import com.official.lockr.domain.club.schedule.domain.ScheduleStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.Attendance;
import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import com.official.lockr.domain.club.schedule.domain.event.AttendanceStatusChangedEvent;
import com.official.lockr.domain.club.schedule.domain.vo.MatchDetailData;
import com.official.lockr.domain.club.schedule.domain.vo.ScheduleDetailData;
import com.official.lockr.domain.club.schedule.domain.vo.SocialDetailData;
import com.official.lockr.domain.club.schedule.domain.vo.TrainingDetailData;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.JSON;
import org.jooq.generated.tables.daos.AttendancesDao;
import org.jooq.generated.tables.daos.SchedulesDao;
import org.jooq.generated.tables.pojos.AttendancesEntity;
import org.jooq.generated.tables.pojos.SchedulesEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.AttendancesJOOQEntity.ATTENDANCES;
import static org.jooq.generated.tables.SchedulesJOOQEntity.SCHEDULES;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQScheduleRepository implements ScheduleRepository {

    private final SchedulesDao schedulesDao;
    private final AttendancesDao attendancesDao;
    private final ObjectMapper objectMapper;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQScheduleRepository(
            final Configuration configuration,
            final ObjectMapper objectMapper,
            final DomainEventPublisher domainEventPublisher
    ) {
        this.schedulesDao = new SchedulesDao(configuration);
        this.attendancesDao = new AttendancesDao(configuration);
        this.objectMapper = objectMapper;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Nullable
    @Override
    public Schedule findById(final String id) {
        final SchedulesEntity entity = schedulesDao.ctx()
                .selectFrom(SCHEDULES)
                .where(SCHEDULES.ID.eq(id))
                .fetchOneInto(SchedulesEntity.class);
        if (isNull(entity)) {
            return null;
        }
        return toDomain(entity);
    }

    @Transactional
    @Override
    public Schedule save(final Schedule schedule) {
        upsertSchedule(schedule);
        syncAttendances(schedule);
        schedule.publish(domainEventPublisher);
        return schedule;
    }

    private void upsertSchedule(final Schedule schedule) {
        final JSON detailData = serializeDetailToJson(schedule.getScheduleType(), schedule.getDetail());

        schedulesDao.ctx()
                .insertInto(SCHEDULES)
                .set(SCHEDULES.ID, schedule.getId())
                .set(SCHEDULES.CLUB_ID, schedule.getClubId())
                .set(SCHEDULES.TITLE, schedule.getTitle())
                .set(SCHEDULES.CONTENT, schedule.getContent())
                .set(SCHEDULES.LOCATION, schedule.getLocation())
                .set(SCHEDULES.SCHEDULE_TIME, schedule.getScheduleTime())
                .set(SCHEDULES.TYPE, schedule.getScheduleType().name())
                .set(SCHEDULES.DETAIL_DATA, detailData)
                .set(SCHEDULES.STATUS, schedule.getStatus().name())
                .set(SCHEDULES.MIN_PARTICIPANTS, schedule.getMinParticipants())
                .set(SCHEDULES.MAX_PARTICIPANTS, schedule.getMaxParticipants())
                .set(SCHEDULES.DEADLINE_DAYS, schedule.getDeadlineDays())
                .set(SCHEDULES.CREATED_AT, schedule.getCreatedAt())
                .set(SCHEDULES.UPDATED_AT, schedule.getUpdatedAt())
                .set(SCHEDULES.DELETED_AT, schedule.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(SCHEDULES.TITLE, excluded(SCHEDULES.TITLE))
                .set(SCHEDULES.CONTENT, excluded(SCHEDULES.CONTENT))
                .set(SCHEDULES.LOCATION, excluded(SCHEDULES.LOCATION))
                .set(SCHEDULES.SCHEDULE_TIME, excluded(SCHEDULES.SCHEDULE_TIME))
                .set(SCHEDULES.DETAIL_DATA, excluded(SCHEDULES.DETAIL_DATA))
                .set(SCHEDULES.STATUS, excluded(SCHEDULES.STATUS))
                .set(SCHEDULES.MIN_PARTICIPANTS, excluded(SCHEDULES.MIN_PARTICIPANTS))
                .set(SCHEDULES.MAX_PARTICIPANTS, excluded(SCHEDULES.MAX_PARTICIPANTS))
                .set(SCHEDULES.DEADLINE_DAYS, excluded(SCHEDULES.DEADLINE_DAYS))
                .set(SCHEDULES.UPDATED_AT, excluded(SCHEDULES.UPDATED_AT))
                .set(SCHEDULES.DELETED_AT, excluded(SCHEDULES.DELETED_AT))
                .execute();
    }

    private void syncAttendances(final Schedule schedule) {
        final String scheduleId = schedule.getId();
        final Map<String, AttendanceStatus> existingStatusMap = loadExistingAttendanceStatuses(scheduleId);
        final List<String> existingUserIds = loadExistingUserIds(scheduleId);
        final List<Attendance> newAttendances = schedule.getAttendances();
        deleteRemovedAttendances(scheduleId, existingUserIds, newAttendances);
        upsertAttendances(scheduleId, existingStatusMap, newAttendances);
    }

    private Map<String, AttendanceStatus> loadExistingAttendanceStatuses(final String scheduleId) {
        return attendancesDao.ctx()
                .select(ATTENDANCES.ID, ATTENDANCES.STATUS)
                .from(ATTENDANCES)
                .where(ATTENDANCES.SCHEDULE_ID.eq(scheduleId))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        record -> record.get(ATTENDANCES.ID),
                        record -> AttendanceStatus.valueOf(record.get(ATTENDANCES.STATUS))
                ));
    }

    private List<String> loadExistingUserIds(final String scheduleId) {
        return attendancesDao.ctx()
                .select(ATTENDANCES.USER_ID)
                .from(ATTENDANCES)
                .where(ATTENDANCES.SCHEDULE_ID.eq(scheduleId))
                .fetch(ATTENDANCES.USER_ID);
    }

    private void deleteRemovedAttendances(final String scheduleId, final List<String> existingUserIds, final List<Attendance> newAttendances) {
        final List<String> newUserIds = newAttendances.stream()
                .map(Attendance::getUserId)
                .toList();

        final List<String> toRemove = existingUserIds.stream()
                .filter(userId -> !newUserIds.contains(userId))
                .toList();

        if (!toRemove.isEmpty()) {
            attendancesDao.ctx()
                    .deleteFrom(ATTENDANCES)
                    .where(ATTENDANCES.SCHEDULE_ID.eq(scheduleId))
                    .and(ATTENDANCES.USER_ID.in(toRemove))
                    .execute();
        }
    }

    private void upsertAttendances(final String scheduleId, final Map<String, AttendanceStatus> existingStatusMap, final List<Attendance> newAttendances) {
        for (Attendance attendance : newAttendances) {
            final AttendanceStatus previousStatus = existingStatusMap.get(attendance.getId());
            final AttendanceStatus newStatus = attendance.getStatus();

            upsertAttendance(scheduleId, attendance);
            publishAttendanceStatusChangeEventIfNeeded(scheduleId, attendance, previousStatus, newStatus);
        }
    }

    private void upsertAttendance(final String scheduleId, final Attendance attendance) {
        attendancesDao.ctx()
                .insertInto(ATTENDANCES)
                .set(ATTENDANCES.ID, attendance.getId())
                .set(ATTENDANCES.SCHEDULE_ID, scheduleId)
                .set(ATTENDANCES.USER_ID, attendance.getUserId())
                .set(ATTENDANCES.STATUS, attendance.getStatus().name())
                .set(ATTENDANCES.REASON, attendance.getReason())
                .set(ATTENDANCES.CREATED_AT, attendance.getCreatedAt())
                .set(ATTENDANCES.UPDATED_AT, attendance.getUpdatedAt())
                .set(ATTENDANCES.DELETED_AT, attendance.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(ATTENDANCES.STATUS, excluded(ATTENDANCES.STATUS))
                .set(ATTENDANCES.REASON, excluded(ATTENDANCES.REASON))
                .set(ATTENDANCES.UPDATED_AT, excluded(ATTENDANCES.UPDATED_AT))
                .set(ATTENDANCES.DELETED_AT, excluded(ATTENDANCES.DELETED_AT))
                .execute();
    }

    private void publishAttendanceStatusChangeEventIfNeeded(final String scheduleId, final Attendance attendance, final AttendanceStatus previousStatus, final AttendanceStatus newStatus) {
        if (previousStatus != null && previousStatus != newStatus) {
            final AttendanceStatusChangedEvent event = AttendanceStatusChangedEvent.of(
                    attendance.getId(),
                    scheduleId,
                    attendance.getUserId(),
                    previousStatus,
                    newStatus
            );
            domainEventPublisher.publish(event);
        }
    }

    private Schedule toDomain(final SchedulesEntity entity) {
        final List<Attendance> attendances = findAttendancesByScheduleId(entity.getId());
        final ScheduleType scheduleType = ScheduleType.valueOf(entity.getType());
        final ScheduleDetailData detail = deserializeDetailFromJson(scheduleType, entity.getDetailData());

        return new Schedule(
                entity.getId(),
                entity.getClubId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getLocation(),
                entity.getScheduleTime(),
                scheduleType,
                detail,
                attendances,
                ScheduleStatus.valueOf(entity.getStatus()),
                entity.getMinParticipants(),
                entity.getMaxParticipants(),
                entity.getDeadlineDays() != null ? entity.getDeadlineDays() : 0,
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
                AttendanceStatus.valueOf(entity.getStatus()),
                entity.getReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private JSON serializeDetailToJson(final ScheduleType scheduleType, final ScheduleDetailData detail) {
        if (detail == null) {
            return null;
        }
        try {
            final String jsonString = switch (scheduleType) {
                case MATCH -> objectMapper.writeValueAsString((MatchDetailData) detail);
                case TRAINING -> objectMapper.writeValueAsString((TrainingDetailData) detail);
                case SOCIAL_EVENT -> objectMapper.writeValueAsString((SocialDetailData) detail);
            };
            return JSON.json(jsonString);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize schedule detail for type: " + scheduleType, e);
        }
    }

    private ScheduleDetailData deserializeDetailFromJson(final ScheduleType scheduleType, final JSON detailData) {
        if (detailData == null) {
            return null;
        }

        try {
            final String jsonString = detailData.data();
            return switch (scheduleType) {
                case MATCH -> objectMapper.readValue(jsonString, MatchDetailData.class);
                case TRAINING -> objectMapper.readValue(jsonString, TrainingDetailData.class);
                case SOCIAL_EVENT -> objectMapper.readValue(jsonString, SocialDetailData.class);
            };
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize schedule detail for type: " + scheduleType, e);
        }
    }
}
