package com.official.lockr.domain.club.schedule.infrastructure;

import com.official.lockr.domain.club.schedule.domain.ScheduleAttendanceHistory;
import com.official.lockr.domain.club.schedule.domain.ScheduleAttendanceHistoryRepository;
import com.official.lockr.domain.club.schedule.domain.event.AttendanceStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ScheduleAttendanceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ScheduleAttendanceEventConsumer.class);

    private final ScheduleAttendanceHistoryRepository scheduleAttendanceHistoryRepository;
    private final RetryTemplate retryTemplate;

    public ScheduleAttendanceEventConsumer(final ScheduleAttendanceHistoryRepository scheduleAttendanceHistoryRepository) {
        this.scheduleAttendanceHistoryRepository = scheduleAttendanceHistoryRepository;
        this.retryTemplate = RetryTemplate.builder()
                .maxAttempts(3)
                .exponentialBackoff(1000, 1.5, 5000)
                .retryOn(Exception.class)
                .build();
    }

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAttendanceStatusChanged(final AttendanceStatusChangedEvent event) {
        try {
            retryTemplate.execute(ctx -> {
                final ScheduleAttendanceHistory history = ScheduleAttendanceHistory.create(
                        event.scheduleId(), event.attendanceId(), event.userId(), event.changedBy(), event.changedByRole(),
                        event.previousStatus(), event.newStatus(), event.reason(), event.changedAt()
                );
                scheduleAttendanceHistoryRepository.save(history);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to save attendance history after all retries. scheduleId={}, userId={}",
                    event.scheduleId(), event.userId(), e);
        }
    }
}
