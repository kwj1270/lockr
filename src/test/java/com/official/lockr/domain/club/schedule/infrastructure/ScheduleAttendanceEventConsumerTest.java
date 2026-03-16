package com.official.lockr.domain.club.schedule.infrastructure;

import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleAttendanceHistory;
import com.official.lockr.domain.club.schedule.domain.ScheduleAttendanceHistoryRepository;
import com.official.lockr.domain.club.schedule.domain.event.AttendanceStatusChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.retry.support.RetryTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScheduleAttendanceEventConsumerTest {

    private ScheduleAttendanceHistoryRepository historyRepository;
    private ScheduleAttendanceEventConsumer consumer;

    @BeforeEach
    void setUp() {
        historyRepository = mock(ScheduleAttendanceHistoryRepository.class);
        consumer = new ScheduleAttendanceEventConsumer(historyRepository, RetryTemplate.builder().maxAttempts(1).build());
    }

    @Test
    @DisplayName("AttendanceStatusChangedEvent 수신 시 ScheduleAttendanceHistory가 저장된다")
    void shouldSaveHistoryWhenEventReceived() {
        // given
        final LocalDateTime changedAt = LocalDateTime.now();
        final AttendanceStatusChangedEvent event = new AttendanceStatusChangedEvent(
                "attendance-001",
                "schedule-001",
                "user-001",
                "admin-001",
                "MANAGER",
                AttendanceStatus.NO_RESPONSE,
                AttendanceStatus.ATTENDING,
                "관리자 출석 처리",
                changedAt
        );

        // when
        consumer.handleAttendanceStatusChanged(event);

        // then
        final ArgumentCaptor<ScheduleAttendanceHistory> captor = ArgumentCaptor.forClass(ScheduleAttendanceHistory.class);
        verify(historyRepository).save(captor.capture());

        final ScheduleAttendanceHistory history = captor.getValue();
        assertThat(history.getScheduleId()).isEqualTo("schedule-001");
        assertThat(history.getAttendanceId()).isEqualTo("attendance-001");
        assertThat(history.getUserId()).isEqualTo("user-001");
        assertThat(history.getChangedBy()).isEqualTo("admin-001");
        assertThat(history.getChangedByRole()).isEqualTo("MANAGER");
        assertThat(history.getPreviousStatus()).isEqualTo(AttendanceStatus.NO_RESPONSE);
        assertThat(history.getNewStatus()).isEqualTo(AttendanceStatus.ATTENDING);
        assertThat(history.getReason()).isEqualTo("관리자 출석 처리");
        assertThat(history.getChangedAt()).isEqualTo(changedAt);
    }

    @Test
    @DisplayName("이벤트 필드가 History 엔티티에 올바르게 매핑된다 - 사용자 본인 변경")
    void shouldMapFieldsCorrectlyForSelfChange() {
        // given
        final LocalDateTime changedAt = LocalDateTime.now();
        final AttendanceStatusChangedEvent event = new AttendanceStatusChangedEvent(
                "attendance-002",
                "schedule-002",
                "user-002",
                "user-002",
                "PLAYER",
                AttendanceStatus.ATTENDING,
                AttendanceStatus.NOT_ATTENDING,
                "개인 사정",
                changedAt
        );

        // when
        consumer.handleAttendanceStatusChanged(event);

        // then
        final ArgumentCaptor<ScheduleAttendanceHistory> captor = ArgumentCaptor.forClass(ScheduleAttendanceHistory.class);
        verify(historyRepository).save(captor.capture());

        final ScheduleAttendanceHistory history = captor.getValue();
        assertThat(history.getUserId()).isEqualTo("user-002");
        assertThat(history.getChangedBy()).isEqualTo("user-002");
        assertThat(history.getChangedByRole()).isEqualTo("PLAYER");
        assertThat(history.getPreviousStatus()).isEqualTo(AttendanceStatus.ATTENDING);
        assertThat(history.getNewStatus()).isEqualTo(AttendanceStatus.NOT_ATTENDING);
    }
}
