package com.official.lockr.domain.notification.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.notification.application.command.MarkAsReadNotificationCommand;
import com.official.lockr.domain.notification.application.usecase.DeleteAllNotificationsUseCase;
import com.official.lockr.domain.notification.application.usecase.DeleteNotificationUseCase;
import com.official.lockr.domain.notification.application.usecase.MarkAsReadNotificationUseCase;
import com.official.lockr.domain.notification.application.usecase.ReadAllNotificationsUseCase;
import com.official.lockr.global.http.HttpHeaders;
import com.official.lockr.global.http.HttpLoggingRepository;
import com.official.lockr.domain.notification.domain.Notification;
import com.official.lockr.domain.notification.domain.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationApi.class)
class NotificationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MarkAsReadNotificationUseCase markAsReadNotificationUseCase;

    @MockBean
    private DeleteNotificationUseCase deleteNotificationUseCase;

    @MockBean
    private DeleteAllNotificationsUseCase deleteAllNotificationsUseCase;

    @MockBean
    private ReadAllNotificationsUseCase readAllNotificationsUseCase;

    @MockBean
    private HttpHeaders httpHeaders;

    @MockBean
    private HttpLoggingRepository httpLoggingRepository;

    @Test
    void shouldMarkNotificationAsRead() throws Exception {
        // given
        final String notificationId = "notification-001";
        final String userId = "user-001";
        final ObjectNode data = objectMapper.createObjectNode();
        final LocalDateTime now = LocalDateTime.now();
        final Notification notification = Notification.from(
                notificationId, userId, "club-001",
                NotificationType.SCHEDULE_LINK_REQUEST,
                "경기 일정 알림", "상대팀이 경기 일정을 등록했습니다.",
                data, true, "CREATE_LINKED_SCHEDULE", "/clubs/club-001/schedules/create",
                now, now, null
        );
        when(markAsReadNotificationUseCase.markAsRead(any(MarkAsReadNotificationCommand.class)))
                .thenReturn(notification);

        // when & then
        mockMvc.perform(post("/api/v1/notifications/{notificationId}/read", notificationId)
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId))
                .andExpect(jsonPath("$.isRead").value(true));
    }

    private SignInSession createSignInSession(String userId) {
        return new SignInSession(
                userId,
                "device-1", "TestDevice", "TestOS",
                "127.0.0.1", "TestAgent",
                LocalDateTime.now()
        );
    }
}
