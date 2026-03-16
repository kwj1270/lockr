package com.official.lockr.domain.club.schedule.api;

import com.official.lockr.domain.club.schedule.domain.ScheduleAttendanceHistory;
import com.official.lockr.domain.club.schedule.domain.ScheduleAttendanceHistoryRepository;
import com.official.lockr.domain.club.schedule.domain.event.AttendanceStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ScheduleAttendanceEventConsumer {

    private final ScheduleAttendanceHistoryRepository scheduleAttendanceHistoryRepository;

    public ScheduleAttendanceEventConsumer(final ScheduleAttendanceHistoryRepository scheduleAttendanceHistoryRepository) {
        this.scheduleAttendanceHistoryRepository = scheduleAttendanceHistoryRepository;
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAttendanceStatusChanged(final AttendanceStatusChangedEvent event) {
        final ScheduleAttendanceHistory history = ScheduleAttendanceHistory.create(
                event.scheduleId(), event.attendanceId(), event.userId(), event.changedBy(), event.changedByRole(),
                event.previousStatus(), event.newStatus(), event.reason(), event.changedAt()
        );
        scheduleAttendanceHistoryRepository.save(history);
    }
}
