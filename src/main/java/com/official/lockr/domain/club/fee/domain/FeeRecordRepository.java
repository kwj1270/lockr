package com.official.lockr.domain.club.fee.domain;

import jakarta.annotation.Nullable;

import java.util.List;

public interface FeeRecordRepository {
    FeeRecord save(FeeRecord feeRecord);

    @Nullable
    FeeRecord findByClubIdAndMemberIdAndYearAndMonth(String clubId, String memberId, int year, int month);

    List<FeeRecord> findByClubIdAndYearAndMonth(String clubId, int year, int month);

    int countNotificationsByClubIdAndYearAndMonth(String clubId, int year, int month);
}
