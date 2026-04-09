package com.official.lockr.domain.notification.infrastructure;

import com.official.lockr.domain.notification.domain.Notification;
import com.official.lockr.domain.notification.domain.NotificationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryNotificationRepository implements NotificationRepository {

    private final Map<String, Notification> notifications = new HashMap<>();

    public void clear() {
        notifications.clear();
    }

    @Override
    public Notification save(final Notification notification) {
        notifications.put(notification.getId(), notification);
        return notification;
    }

    @Override
    public Notification findById(final String id) {
        return notifications.get(id);
    }

    @Override
    public List<Notification> findByUserId(final String userId) {
        return notifications.values().stream()
                .filter(n -> n.getUserId().equals(userId))
                .filter(n -> n.getDeletedAt() == null)
                .toList();
    }

    @Override
    public List<Notification> findByUserIdAndClubId(final String userId, final String clubId) {
        return notifications.values().stream()
                .filter(n -> n.getUserId().equals(userId))
                .filter(n -> n.getClubId() != null && n.getClubId().equals(clubId))
                .filter(n -> n.getDeletedAt() == null)
                .toList();
    }

    @Override
    public void softDeleteAllByUserId(final String userId) {
        notifications.values().stream()
                .filter(n -> n.getUserId().equals(userId))
                .filter(n -> n.getDeletedAt() == null)
                .forEach(Notification::softDelete);
    }

    @Override
    public void readAllByUserId(final String userId) {
        notifications.values().stream()
                .filter(n -> n.getUserId().equals(userId))
                .filter(n -> !n.isRead())
                .filter(n -> n.getDeletedAt() == null)
                .forEach(Notification::markAsRead);
    }
}
