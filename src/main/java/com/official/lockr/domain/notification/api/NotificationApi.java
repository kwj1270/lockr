package com.official.lockr.domain.notification.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.notification.api.dto.NotificationResponse;
import com.official.lockr.domain.notification.api.dto.RegisterFcmTokenRequest;
import com.official.lockr.domain.notification.application.command.DeleteAllNotificationsCommand;
import com.official.lockr.domain.notification.application.command.DeleteFcmTokenCommand;
import com.official.lockr.domain.notification.application.command.DeleteNotificationCommand;
import com.official.lockr.domain.notification.application.command.MarkAsReadNotificationCommand;
import com.official.lockr.domain.notification.application.command.ReadAllNotificationsCommand;
import com.official.lockr.domain.notification.application.usecase.DeleteAllNotificationsUseCase;
import com.official.lockr.domain.notification.application.usecase.DeleteFcmTokenUseCase;
import com.official.lockr.domain.notification.application.usecase.DeleteNotificationUseCase;
import com.official.lockr.domain.notification.application.usecase.MarkAsReadNotificationUseCase;
import com.official.lockr.domain.notification.application.usecase.ReadAllNotificationsUseCase;
import com.official.lockr.domain.notification.application.usecase.RegisterFcmTokenUseCase;
import com.official.lockr.domain.notification.domain.Notification;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final RegisterFcmTokenUseCase registerFcmTokenUseCase;
    private final DeleteFcmTokenUseCase deleteFcmTokenUseCase;

    public NotificationApi(
            final MarkAsReadNotificationUseCase markAsReadNotificationUseCase,
            final DeleteNotificationUseCase deleteNotificationUseCase,
            final DeleteAllNotificationsUseCase deleteAllNotificationsUseCase,
            final ReadAllNotificationsUseCase readAllNotificationsUseCase,
            final RegisterFcmTokenUseCase registerFcmTokenUseCase,
            final DeleteFcmTokenUseCase deleteFcmTokenUseCase
    ) {
        this.markAsReadNotificationUseCase = markAsReadNotificationUseCase;
        this.deleteNotificationUseCase = deleteNotificationUseCase;
        this.deleteAllNotificationsUseCase = deleteAllNotificationsUseCase;
        this.readAllNotificationsUseCase = readAllNotificationsUseCase;
        this.registerFcmTokenUseCase = registerFcmTokenUseCase;
        this.deleteFcmTokenUseCase = deleteFcmTokenUseCase;
    }

    @PostMapping("/notifications/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable final String notificationId,
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        final Notification notification = markAsReadNotificationUseCase.markAsRead(
                new MarkAsReadNotificationCommand(notificationId, signInSession.userId())
        );
        return ResponseEntity.ok(NotificationResponse.from(notification));
    }

    @PostMapping("/notifications/{notificationId}/delete")
    public ResponseEntity<Void> delete(
            @PathVariable final String notificationId,
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        deleteNotificationUseCase.delete(new DeleteNotificationCommand(notificationId, signInSession.userId()));
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

    @PostMapping("/notifications/fcm-token")
    public ResponseEntity<Void> registerFcmToken(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @Valid @RequestBody final RegisterFcmTokenRequest request
    ) {
        registerFcmTokenUseCase.register(request.toCommand(signInSession.userId()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/notifications/fcm-token/{token}")
    public ResponseEntity<Void> deleteFcmToken(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String token
    ) {
        deleteFcmTokenUseCase.delete(new DeleteFcmTokenCommand(signInSession.userId(), token));
        return ResponseEntity.ok().build();
    }
}
