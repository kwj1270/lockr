package com.official.lockr.domain.club.schedule.infrastructure;

import com.official.lockr.domain.club.schedule.domain.ScheduleAttendanceHistory;
import com.official.lockr.domain.club.schedule.domain.ScheduleAttendanceHistoryRepository;
import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ScheduleAttendanceHistoriesDao;
import org.jooq.generated.tables.pojos.ScheduleAttendanceHistoriesEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.jooq.generated.tables.ScheduleAttendanceHistoriesJOOQEntity.SCHEDULE_ATTENDANCE_HISTORIES;

@Repository
public class JOOQScheduleAttendanceHistoryRepository implements ScheduleAttendanceHistoryRepository {

    private final ScheduleAttendanceHistoriesDao scheduleAttendanceHistoriesDao;

    public JOOQScheduleAttendanceHistoryRepository(final Configuration configuration) {
        this.scheduleAttendanceHistoriesDao = new ScheduleAttendanceHistoriesDao(configuration);
    }

    @Transactional
    @Override
    public ScheduleAttendanceHistory save(final ScheduleAttendanceHistory history) {
        scheduleAttendanceHistoriesDao.ctx()
                .insertInto(SCHEDULE_ATTENDANCE_HISTORIES)
                .set(SCHEDULE_ATTENDANCE_HISTORIES.ID, history.getId())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.SCHEDULE_ID, history.getScheduleId())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.ATTENDANCE_ID, history.getAttendanceId())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.USER_ID, history.getUserId())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.CHANGED_BY, history.getChangedBy())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.CHANGED_BY_ROLE, history.getChangedByRole())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.PREVIOUS_STATUS, history.getPreviousStatus().name())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.NEW_STATUS, history.getNewStatus().name())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.REASON, history.getReason())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.CHANGED_AT, history.getChangedAt())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.CREATED_AT, history.getCreatedAt())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.UPDATED_AT, history.getUpdatedAt())
                .set(SCHEDULE_ATTENDANCE_HISTORIES.DELETED_AT, history.getDeletedAt())
                .execute();
        return history;
    }

    private ScheduleAttendanceHistory toDomain(final ScheduleAttendanceHistoriesEntity entity) {
        return new ScheduleAttendanceHistory(
                entity.getId(),
                entity.getScheduleId(),
                entity.getAttendanceId(),
                entity.getUserId(),
                entity.getChangedBy(),
                entity.getChangedByRole(),
                AttendanceStatus.valueOf(entity.getPreviousStatus()),
                AttendanceStatus.valueOf(entity.getNewStatus()),
                entity.getReason(),
                entity.getChangedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
