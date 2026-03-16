package com.official.lockr.domain.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.official.lockr.domain.notification.application.command.CreateScheduleLinkNotificationCommand;
import com.official.lockr.domain.notification.application.command.DeleteAllNotificationsCommand;
import com.official.lockr.domain.notification.application.command.DeleteNotificationCommand;
import com.official.lockr.domain.notification.application.command.MarkAsReadNotificationCommand;
import com.official.lockr.domain.notification.application.command.ReadAllNotificationsCommand;
import com.official.lockr.domain.notification.application.usecase.CreateScheduleLinkNotificationUseCase;
import com.official.lockr.domain.notification.application.usecase.DeleteAllNotificationsUseCase;
import com.official.lockr.domain.notification.application.usecase.DeleteNotificationUseCase;
import com.official.lockr.domain.notification.application.usecase.MarkAsReadNotificationUseCase;
import com.official.lockr.domain.notification.application.usecase.ReadAllNotificationsUseCase;
import com.official.lockr.domain.notification.domain.Notification;
import com.official.lockr.domain.notification.domain.NotificationRepository;
import com.official.lockr.domain.notification.domain.NotificationTargetQuery;
import com.official.lockr.domain.notification.domain.NotificationType;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;

@Service
public class NotificationService implements CreateScheduleLinkNotificationUseCase, MarkAsReadNotificationUseCase,
        DeleteNotificationUseCase, DeleteAllNotificationsUseCase, ReadAllNotificationsUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationTargetQuery notificationTargetQuery;
    private final ObjectMapper objectMapper;

    public NotificationService(
            final NotificationRepository notificationRepository,
            final NotificationTargetQuery notificationTargetQuery,
            final ObjectMapper objectMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationTargetQuery = notificationTargetQuery;
        this.objectMapper = objectMapper;
    }

    @Override
    public void create(final CreateScheduleLinkNotificationCommand command) {
        final List<String> staffUserIds = notificationTargetQuery.findStaffUserIdsByClubId(command.targetClubId());
        if (staffUserIds.isEmpty()) {
            return;
        }

        final ObjectNode data = objectMapper.createObjectNode();
        data.put("sourceScheduleId", command.sourceScheduleId());
        data.put("sourceClubId", command.sourceClubId());
        data.put("sourceClubName", command.sourceClubName());
        data.put("scheduleTitle", command.scheduleTitle());
        data.put("scheduleTime", command.scheduleTime());

        for (final String userId : staffUserIds) {
            final Notification notification = Notification.init(
                    userId,
                    command.targetClubId(),
                    NotificationType.SCHEDULE_LINK_REQUEST,
                    command.sourceClubName() + "팀과의 경기 일정",
                    command.sourceClubName() + "팀이 " + command.scheduleTime() + " 경기 일정을 등록했습니다. 우리 팀 일정도 만들어 보세요!",
                    data,
                    "CREATE_LINKED_SCHEDULE",
                    "/clubs/" + command.targetClubId() + "/schedules/create-from-notification"
            );
            notificationRepository.save(notification);
        }
    }

    @Override
    public Notification markAsRead(final MarkAsReadNotificationCommand command) {
        final Notification notification = notificationRepository.findById(command.notificationId());
        if (isNull(notification)) {
            throw new IllegalArgumentException("Notification not found: " + command.notificationId());
        }
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Override
    public void delete(final DeleteNotificationCommand command) {
        final Notification notification = notificationRepository.findById(command.notificationId());
        if (isNull(notification)) {
            throw new IllegalArgumentException("Notification not found: " + command.notificationId());
        }
        notification.softDelete();
        notificationRepository.save(notification);
    }

    @Override
    public void deleteAll(final DeleteAllNotificationsCommand command) {
        notificationRepository.softDeleteAllByUserId(command.userId());
    }

    @Override
    public void readAll(final ReadAllNotificationsCommand command) {
        notificationRepository.readAllByUserId(command.userId());
    }

    public void createScheduleLinkNotification(
            final String targetClubId,
            final String sourceScheduleId,
            final String sourceClubId,
            final String sourceClubName,
            final String scheduleTitle,
            final String scheduleTime
    ) {
        create(new CreateScheduleLinkNotificationCommand(
                targetClubId, sourceScheduleId, sourceClubId, sourceClubName, scheduleTitle, scheduleTime
        ));
    }

    public List<Notification> getUserNotifications(final String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<Notification> getClubNotifications(final String userId, final String clubId) {
        return notificationRepository.findByUserIdAndClubId(userId, clubId);
    }

    public Notification markAsRead(final String notificationId) {
        return markAsRead(new MarkAsReadNotificationCommand(notificationId));
    }
}
