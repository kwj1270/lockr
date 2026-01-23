package com.official.lockr.domain.notification.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.notification.api.dto.NotificationResponse;
import com.official.lockr.domain.notification.api.dto.NotificationsResponse;
import jakarta.servlet.http.HttpSession;
import org.jooq.Configuration;
import org.jooq.JSON;
import org.jooq.generated.tables.daos.NotificationsDao;
import org.jooq.generated.tables.pojos.NotificationsEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.NotificationsJOOQEntity.NOTIFICATIONS;

@RestController
@RequestMapping("/api/v1")
public class NotificationQueryApi {

    private final NotificationsDao notificationsDao;
    private final ObjectMapper objectMapper;

    public NotificationQueryApi(final Configuration configuration, final ObjectMapper objectMapper) {
        this.notificationsDao = new NotificationsDao(configuration);
        this.objectMapper = objectMapper;
    }

    @GetMapping("/notifications")
    public ResponseEntity<NotificationsResponse> getNotifications(final HttpSession httpSession) {
        final SignInSession session = session(httpSession);
        final List<NotificationResponse> notifications = notificationsDao.ctx()
                .selectFrom(NOTIFICATIONS)
                .where(NOTIFICATIONS.USER_ID.eq(session.userId()))
                .and(NOTIFICATIONS.DELETED_AT.isNull())
                .orderBy(NOTIFICATIONS.CREATED_AT.desc())
                .fetchInto(NotificationsEntity.class)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(new NotificationsResponse(notifications));
    }

    @GetMapping("/clubs/{clubId}/notifications")
    public ResponseEntity<NotificationsResponse> getClubNotifications(
            @PathVariable final String clubId,
            final HttpSession httpSession
    ) {
        final SignInSession session = session(httpSession);
        final List<NotificationResponse> notifications = notificationsDao.ctx()
                .selectFrom(NOTIFICATIONS)
                .where(NOTIFICATIONS.USER_ID.eq(session.userId()))
                .and(NOTIFICATIONS.CLUB_ID.eq(clubId))
                .and(NOTIFICATIONS.DELETED_AT.isNull())
                .orderBy(NOTIFICATIONS.CREATED_AT.desc())
                .fetchInto(NotificationsEntity.class)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(new NotificationsResponse(notifications));
    }

    private NotificationResponse toResponse(final NotificationsEntity entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getClubId(),
                entity.getType(),
                entity.getTitle(),
                entity.getContent(),
                deserializeData(entity.getData()),
                entity.getIsRead(),
                entity.getActionType(),
                entity.getActionUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private JsonNode deserializeData(final JSON data) {
        if (isNull(data)) {
            return null;
        }
        try {
            return objectMapper.readTree(data.data());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize notification data", e);
        }
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
