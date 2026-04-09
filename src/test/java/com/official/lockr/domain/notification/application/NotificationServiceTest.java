package com.official.lockr.domain.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.official.lockr.domain.notification.application.command.CreateScheduleLinkNotificationCommand;
import com.official.lockr.domain.notification.application.command.MarkAsReadNotificationCommand;
import com.official.lockr.domain.notification.domain.Notification;
import com.official.lockr.domain.notification.domain.NotificationRepository;
import com.official.lockr.domain.notification.domain.NotificationTargetQuery;
import com.official.lockr.domain.notification.domain.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTargetQuery notificationTargetQuery;

    private NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, notificationTargetQuery, objectMapper);
    }

    @Test
    void shouldMarkNotificationAsReadUsingCommand() {
        // given
        final String notificationId = "notification-001";
        final ObjectNode data = objectMapper.createObjectNode();
        final var now = LocalDateTime.now();
        final Notification notification = Notification.from(
                notificationId, "user-001", "club-001",
                NotificationType.SCHEDULE_LINK_REQUEST,
                "알림1", "내용1", data, false, "ACTION", "/action", now, now, null
        );
        when(notificationRepository.findById(notificationId)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        final Notification result = notificationService.markAsRead(new MarkAsReadNotificationCommand(notificationId, "user-001"));

        // then
        assertThat(result.isRead()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenNotificationNotFound() {
        // given
        final String notificationId = "non-existent-id";
        when(notificationRepository.findById(notificationId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(new MarkAsReadNotificationCommand(notificationId, "any-user")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Notification not found: " + notificationId);
    }

    @Test
    void shouldThrowExceptionWhenUserIdMismatch() {
        // given
        final String notificationId = "notification-001";
        final ObjectNode data = objectMapper.createObjectNode();
        final var now = LocalDateTime.now();
        final Notification notification = Notification.from(
                notificationId, "user-001", "club-001",
                NotificationType.SCHEDULE_LINK_REQUEST,
                "알림1", "내용1", data, false, "ACTION", "/action", now, now, null
        );
        when(notificationRepository.findById(notificationId)).thenReturn(notification);

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(new MarkAsReadNotificationCommand(notificationId, "other-user")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Notification does not belong to user: other-user");
    }

    @Test
    void shouldCreateScheduleLinkNotificationUsingCommand() {
        // given
        final String targetClubId = "target-club-001";
        final String sourceScheduleId = "schedule-001";
        final String sourceClubId = "source-club-001";
        final String sourceClubName = "FC서울";
        final String scheduleTitle = "친선경기";
        final String scheduleTime = "2026-01-20 15:00";

        when(notificationTargetQuery.findStaffUserIdsByClubId(targetClubId)).thenReturn(List.of("staff-user-001"));

        // when
        notificationService.create(new CreateScheduleLinkNotificationCommand(
                targetClubId, sourceScheduleId, sourceClubId, sourceClubName, scheduleTitle, scheduleTime
        ));

        // then
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void shouldNotCreateNotificationWhenTargetClubNotExists() {
        // given
        final String targetClubId = "non-existent-club";
        when(notificationTargetQuery.findStaffUserIdsByClubId(targetClubId)).thenReturn(List.of());

        // when
        notificationService.create(new CreateScheduleLinkNotificationCommand(
                targetClubId, "schedule-001", "source-club-001", "FC서울", "친선경기", "2026-01-20 15:00"
        ));

        // then
        verify(notificationRepository, times(0)).save(any(Notification.class));
    }
}
