package com.official.lockr.domain.notification.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.notification.api.dto.NotificationResponse;
import com.official.lockr.domain.notification.application.command.DeleteAllNotificationsCommand;
import com.official.lockr.domain.notification.application.command.DeleteNotificationCommand;
import com.official.lockr.domain.notification.application.command.MarkAsReadNotificationCommand;
import com.official.lockr.domain.notification.application.command.ReadAllNotificationsCommand;
import com.official.lockr.domain.notification.application.usecase.DeleteAllNotificationsUseCase;
import com.official.lockr.domain.notification.application.usecase.DeleteNotificationUseCase;
import com.official.lockr.domain.notification.application.usecase.MarkAsReadNotificationUseCase;
import com.official.lockr.domain.notification.application.usecase.ReadAllNotificationsUseCase;
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
    private final DeleteNotificationUseCase deleteNotificationUseCase;
    private final DeleteAllNotificationsUseCase deleteAllNotificationsUseCase;
    private final ReadAllNotificationsUseCase readAllNotificationsUseCase;

    public NotificationApi(
            final MarkAsReadNotificationUseCase markAsReadNotificationUseCase,
            final DeleteNotificationUseCase deleteNotificationUseCase,
            final DeleteAllNotificationsUseCase deleteAllNotificationsUseCase,
            final ReadAllNotificationsUseCase readAllNotificationsUseCase
    ) {
        this.markAsReadNotificationUseCase = markAsReadNotificationUseCase;
        this.deleteNotificationUseCase = deleteNotificationUseCase;
        this.deleteAllNotificationsUseCase = deleteAllNotificationsUseCase;
        this.readAllNotificationsUseCase = readAllNotificationsUseCase;
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

    @PostMapping("/notifications/{notificationId}/delete")
    public ResponseEntity<Void> delete(
            @PathVariable final String notificationId,
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        deleteNotificationUseCase.delete(new DeleteNotificationCommand(notificationId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications/delete-all")
    public ResponseEntity<Void> deleteAll(
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        deleteAllNotificationsUseCase.deleteAll(new DeleteAllNotificationsCommand(signInSession.userId()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications/read-all")
    public ResponseEntity<Void> readAll(
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        readAllNotificationsUseCase.readAll(new ReadAllNotificationsCommand(signInSession.userId()));
        return ResponseEntity.ok().build();
    }
}
