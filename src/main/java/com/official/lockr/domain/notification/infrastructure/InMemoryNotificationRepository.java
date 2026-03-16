package com.official.lockr.domain.notification.infrastructure;

import com.official.lockr.domain.notification.domain.Notification;
import com.official.lockr.domain.notification.domain.NotificationRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InMemoryNotificationRepository implements NotificationRepository {

    @Override
    public Notification save(final Notification notification) {
        return null;
    }

    @Override
    public Notification findById(final String id) {
        return null;
    }

    @Override
    public List<Notification> findByUserId(final String userId) {
        return List.of();
    }

    @Override
    public List<Notification> findByUserIdAndClubId(final String userId, final String clubId) {
        return List.of();
    }
}
