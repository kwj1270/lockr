package com.official.lockr.domain.notification.domain;

import jakarta.annotation.Nullable;

import java.util.List;

public interface NotificationRepository {
    Notification save(Notification notification);

    @Nullable
    Notification findById(String id);

    List<Notification> findByUserId(String userId);

    List<Notification> findByUserIdAndClubId(String userId, String clubId);
}
