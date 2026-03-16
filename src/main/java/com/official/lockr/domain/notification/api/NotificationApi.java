package com.official.lockr.domain.notification.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.notification.api.dto.NotificationResponse;
import com.official.lockr.domain.notification.application.command.MarkAsReadNotificationCommand;
import com.official.lockr.domain.notification.application.usecase.MarkAsReadNotificationUseCase;
import com.official.lockr.domain.notification.domain.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
@RestController
public class NotificationApi {

    private final MarkAsReadNotificationUseCase markAsReadNotificationUseCase;

    public NotificationApi(final MarkAsReadNotificationUseCase markAsReadNotificationUseCase) {
        this.markAsReadNotificationUseCase = markAsReadNotificationUseCase;
    }

    @PostMapping("/notifications/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable final String notificationId,
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        final Notification notification = markAsReadNotificationUseCase.markAsRead(
                new MarkAsReadNotificationCommand(notificationId)
        );
        return ResponseEntity.ok(NotificationResponse.from(notification));
    }
}
