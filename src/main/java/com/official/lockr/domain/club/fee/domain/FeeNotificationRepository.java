package com.official.lockr.domain.club.fee.domain;

public interface FeeNotificationRepository {
    FeeNotification save(FeeNotification notification);
    int countByClubIdAndYearAndMonth(String clubId, int year, int month);
}
