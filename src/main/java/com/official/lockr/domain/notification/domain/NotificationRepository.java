package com.official.lockr.domain.notification.domain;

import java.util.List;

public interface NotificationRepository {
    Notification save(Notification notification);

    Notification findById(String id);

    List<Notification> findByUserId(String userId);

    List<Notification> findByUserIdAndClubId(String userId, String clubId);

    void softDeleteAllByUserId(String userId);

    void readAllByUserId(String userId);
}
