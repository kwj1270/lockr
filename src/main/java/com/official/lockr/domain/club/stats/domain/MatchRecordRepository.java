package com.official.lockr.domain.club.stats.domain;

import jakarta.annotation.Nullable;

import java.util.List;

public interface MatchRecordRepository {

    MatchRecord save(MatchRecord matchRecord);

    @Nullable
    MatchRecord findById(String id);

    List<MatchRecord> findAllByClubId(String clubId);

    List<MatchRecord> findAllByClubIdAndSeason(String clubId, String season);

    @Nullable
    MatchRecord findByScheduleId(String scheduleId);

    void delete(MatchRecord matchRecord);
}
