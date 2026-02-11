package com.official.lockr.domain.club.stats.domain.event;

import com.official.lockr.domain.club.stats.domain.MatchResult;
import com.official.lockr.domain.club.stats.domain.MatchScore;
import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDate;

public record UpdatedMatchEvent(
        String matchRecordId,
        String clubId,
        LocalDate matchDate,
        String opponentName,
        MatchScore score,
        MatchResult result,
        String season
) implements DomainEvent {
}
