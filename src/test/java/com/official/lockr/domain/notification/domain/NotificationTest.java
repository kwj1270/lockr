package com.official.lockr.domain.notification.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateNotificationUsingInit() {
        // given
        final String userId = "user-001";
        final String clubId = "club-001";
        final NotificationType type = NotificationType.SCHEDULE_LINK_REQUEST;
        final String title = "경기 일정 알림";
        final String content = "상대팀이 경기 일정을 등록했습니다.";
        final ObjectNode data = objectMapper.createObjectNode();
        data.put("sourceScheduleId", "schedule-001");
        final String actionType = "CREATE_LINKED_SCHEDULE";
        final String actionUrl = "/clubs/club-001/schedules/create";

        // when
        final Notification notification = Notification.init(
                userId, clubId, type, title, content, data, actionType, actionUrl
        );

        // then
        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getClubId()).isEqualTo(clubId);
        assertThat(notification.getType()).isEqualTo(type);
        assertThat(notification.getTitle()).isEqualTo(title);
        assertThat(notification.getContent()).isEqualTo(content);
        assertThat(notification.getData()).isEqualTo(data);
        assertThat(notification.getActionType()).isEqualTo(actionType);
        assertThat(notification.getActionUrl()).isEqualTo(actionUrl);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getUpdatedAt()).isNotNull();
        assertThat(notification.getDeletedAt()).isNull();
    }

    @Test
    void shouldMarkNotificationAsRead() {
        // given
        final ObjectNode data = objectMapper.createObjectNode();
        final Notification notification = Notification.init(
                "user-001",
                "club-001",
                NotificationType.SCHEDULE_LINK_REQUEST,
                "경기 일정 알림",
                "상대팀이 경기 일정을 등록했습니다.",
                data,
                "CREATE_LINKED_SCHEDULE",
                "/clubs/club-001/schedules/create"
        );
        final var originalUpdatedAt = notification.getUpdatedAt();

        // when
        notification.markAsRead();

        // then
        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }

    @Test
    void shouldReconstituteNotificationUsingFrom() {
        // given
        final String id = "notification-001";
        final String userId = "user-001";
        final String clubId = "club-001";
        final NotificationType type = NotificationType.SCHEDULE_LINK_REQUEST;
        final String title = "경기 일정 알림";
        final String content = "상대팀이 경기 일정을 등록했습니다.";
        final ObjectNode data = objectMapper.createObjectNode();
        data.put("sourceScheduleId", "schedule-001");
        final boolean isRead = true;
        final String actionType = "CREATE_LINKED_SCHEDULE";
        final String actionUrl = "/clubs/club-001/schedules/create";
        final var now = java.time.LocalDateTime.now();

        // when
        final Notification notification = Notification.from(
                id, userId, clubId, type, title, content, data, isRead, actionType, actionUrl, now, now, null
        );

        // then
        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getClubId()).isEqualTo(clubId);
        assertThat(notification.getType()).isEqualTo(type);
        assertThat(notification.getTitle()).isEqualTo(title);
        assertThat(notification.getContent()).isEqualTo(content);
        assertThat(notification.getData()).isEqualTo(data);
        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getActionType()).isEqualTo(actionType);
        assertThat(notification.getActionUrl()).isEqualTo(actionUrl);
    }

    @Test
    void shouldBeEqualWhenIdIsSame() {
        // given
        final ObjectNode data = objectMapper.createObjectNode();
        final var now = java.time.LocalDateTime.now();
        final Notification notification1 = Notification.from(
                "notification-001", "user-001", "club-001",
                NotificationType.SCHEDULE_LINK_REQUEST,
                "알림1", "내용1", data, false, "ACTION", "/action", now, now, null
        );
        final Notification notification2 = Notification.from(
                "notification-001", "user-002", "club-002",
                NotificationType.SCHEDULE_UPDATED,
                "알림2", "내용2", data, true, "OTHER", "/other", now, now, null
        );

        // then
        assertThat(notification1).isEqualTo(notification2);
        assertThat(notification1.hashCode()).isEqualTo(notification2.hashCode());
    }
}
