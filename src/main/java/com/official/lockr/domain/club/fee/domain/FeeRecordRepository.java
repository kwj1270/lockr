package com.official.lockr.domain.club.fee.domain;

import java.util.List;

public interface FeeRecordRepository {
    FeeRecord save(FeeRecord feeRecord);

    /** @return null if not found */
    FeeRecord findByClubIdAndMemberIdAndYearAndMonth(String clubId, String memberId, int year, int month);

    List<FeeRecord> findByClubIdAndYearAndMonth(String clubId, int year, int month);
}
