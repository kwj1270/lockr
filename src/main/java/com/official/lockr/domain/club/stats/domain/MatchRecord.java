package com.official.lockr.domain.club.stats.domain;

import com.official.lockr.domain.club.stats.domain.event.DeletedMatchEvent;
import com.official.lockr.domain.club.stats.domain.event.RecordedMatchEvent;
import com.official.lockr.domain.club.stats.domain.event.UpdatedMatchEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MatchRecord extends AggregateRoot {

    private final String id;
    private final String clubId;
    private final String scheduleId;
    private final LocalDate matchDate;
    private final String opponentName;
    private final MatchScore score;
    private final MatchResult result;
    private final String recordedBy;
    private final String season;
    private final List<PlayerPerformance> playerPerformances;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public MatchRecord(final String id, final String clubId, final String scheduleId,
                       final LocalDate matchDate, final String opponentName,
                       final MatchScore score, final MatchResult result,
                       final String recordedBy, final String season,
                       final List<PlayerPerformance> playerPerformances,
                       final LocalDateTime createdAt, final LocalDateTime updatedAt,
                       final LocalDateTime deletedAt) {
        this.id = id;
        this.clubId = clubId;
        this.scheduleId = scheduleId;
        this.matchDate = matchDate;
        this.opponentName = opponentName;
        this.score = score;
        this.result = result;
        this.recordedBy = recordedBy;
        this.season = season;
        this.playerPerformances = playerPerformances;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static MatchRecord create(final String id, final String clubId, final String scheduleId,
                                     final LocalDate matchDate, final String opponentName,
                                     final MatchScore score, final String recordedBy,
                                     final String season) {
        final MatchResult result = MatchResult.from(score);
        final LocalDateTime now = LocalDateTime.now();

        final MatchRecord record = new MatchRecord(
                id, clubId, scheduleId, matchDate, opponentName, score, result,
                recordedBy, season, new ArrayList<>(), now, now, null
        );

        record.addEvent(new RecordedMatchEvent(
                id, clubId, matchDate, opponentName, score, result, season
        ));

        return record;
    }

    public void addPlayerPerformance(final PlayerPerformance performance) {
        this.playerPerformances.add(performance);
    }

    public MatchRecord update(final LocalDate newMatchDate, final String newOpponentName,
                              final MatchScore newScore,
                              final List<PlayerPerformance> newPlayerPerformances) {
        final MatchResult newResult = MatchResult.from(newScore);
        final MatchRecord updated = new MatchRecord(
                this.id,
                this.clubId,
                this.scheduleId,
                newMatchDate,
                newOpponentName,
                newScore,
                newResult,
                this.recordedBy,
                this.season,
                new ArrayList<>(newPlayerPerformances),
                this.createdAt,
                LocalDateTime.now(),
                this.deletedAt
        );

        updated.addEvent(new UpdatedMatchEvent(
                this.id, this.clubId, newMatchDate, newOpponentName, newScore, newResult, this.season
        ));

        return updated;
    }

    public MatchRecord delete() {
        final MatchRecord deleted = new MatchRecord(
                this.id,
                this.clubId,
                this.scheduleId,
                this.matchDate,
                this.opponentName,
                this.score,
                this.result,
                this.recordedBy,
                this.season,
                this.playerPerformances,
                this.createdAt,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        deleted.addEvent(new DeletedMatchEvent(this.id, this.clubId, this.season));

        return deleted;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public String getId() {
        return id;
    }

    public String getClubId() {
        return clubId;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public MatchScore getScore() {
        return score;
    }

    public MatchResult getResult() {
        return result;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public String getSeason() {
        return season;
    }

    public List<PlayerPerformance> getPlayerPerformances() {
        return List.copyOf(playerPerformances);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
