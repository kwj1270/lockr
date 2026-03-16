package com.official.lockr.domain.notification.domain;

import java.util.List;

public interface NotificationTargetQuery {
    List<String> findStaffUserIdsByClubId(String clubId);
}
