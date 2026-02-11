package com.official.lockr.domain.club.stats.domain;

import com.official.lockr.domain.club.stats.domain.event.DeletedMatchEvent;
import com.official.lockr.domain.club.stats.domain.event.RecordedMatchEvent;
import com.official.lockr.domain.club.stats.domain.event.UpdatedMatchEvent;
import com.official.lockr.global.ddd.DomainEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchRecordTest {

    // Phase 1: MatchScore tests
    @Test
    void shouldThrowExceptionWhenOurScoreIsNegative() {
        // when & then
        assertThatThrownBy(() -> new MatchScore(-1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void shouldThrowExceptionWhenOpponentScoreIsNegative() {
        // when & then
        assertThatThrownBy(() -> new MatchScore(0, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void shouldCreateMatchScoreWithValidScores() {
        // when
        final MatchScore score = new MatchScore(3, 1);

        // then
        assertThat(score.ourScore()).isEqualTo(3);
        assertThat(score.opponentScore()).isEqualTo(1);
    }

    // Phase 1: MatchResult tests
    @Test
    void shouldReturnWinWhenOurScoreIsGreater() {
        // given
        final MatchScore score = new MatchScore(3, 1);

        // when
        final MatchResult result = MatchResult.from(score);

        // then
        assertThat(result).isEqualTo(MatchResult.WIN);
    }

    @Test
    void shouldReturnLoseWhenOpponentScoreIsGreater() {
        // given
        final MatchScore score = new MatchScore(1, 3);

        // when
        final MatchResult result = MatchResult.from(score);

        // then
        assertThat(result).isEqualTo(MatchResult.LOSE);
    }

    @Test
    void shouldReturnDrawWhenScoresAreEqual() {
        // given
        final MatchScore score = new MatchScore(2, 2);

        // when
        final MatchResult result = MatchResult.from(score);

        // then
        assertThat(result).isEqualTo(MatchResult.DRAW);
    }

    // Phase 1: MatchRecord tests
    @Test
    void shouldCreateMatchRecordWithRequiredFields() {
        // given
        final String id = "match-001";
        final String clubId = "club-001";
        final LocalDate matchDate = LocalDate.now();
        final String opponentName = "상대팀FC";
        final MatchScore score = new MatchScore(2, 1);
        final String recordedBy = "user-001";
        final String season = "2025";

        // when
        final MatchRecord record = MatchRecord.create(
                id, clubId, null, matchDate, opponentName, score, recordedBy, season
        );

        // then
        assertThat(record.getId()).isEqualTo(id);
        assertThat(record.getClubId()).isEqualTo(clubId);
        assertThat(record.getMatchDate()).isEqualTo(matchDate);
        assertThat(record.getOpponentName()).isEqualTo(opponentName);
        assertThat(record.getScore()).isEqualTo(score);
        assertThat(record.getResult()).isEqualTo(MatchResult.WIN);
        assertThat(record.getRecordedBy()).isEqualTo(recordedBy);
        assertThat(record.getSeason()).isEqualTo(season);
        assertThat(record.getPlayerPerformances()).isEmpty();
    }

    // Phase 1: PlayerPerformance tests
    @Test
    void shouldCreatePlayerPerformanceWithValidData() {
        // given
        final String id = "perf-001";
        final String matchRecordId = "match-001";
        final String clubId = "club-001";
        final String userId = "user-001";
        final int goals = 2;
        final int assists = 1;
        final boolean isMom = true;
        final Integer minutesPlayed = 90;

        // when
        final PlayerPerformance performance = new PlayerPerformance(
                id, matchRecordId, clubId, userId, goals, assists, isMom, minutesPlayed
        );

        // then
        assertThat(performance.getId()).isEqualTo(id);
        assertThat(performance.getMatchRecordId()).isEqualTo(matchRecordId);
        assertThat(performance.getClubId()).isEqualTo(clubId);
        assertThat(performance.getUserId()).isEqualTo(userId);
        assertThat(performance.getGoals()).isEqualTo(goals);
        assertThat(performance.getAssists()).isEqualTo(assists);
        assertThat(performance.isMom()).isTrue();
        assertThat(performance.getMinutesPlayed()).isEqualTo(minutesPlayed);
    }

    @Test
    void shouldThrowExceptionWhenGoalsAreNegative() {
        // when & then
        assertThatThrownBy(() -> new PlayerPerformance(
                "perf-001", "match-001", "club-001", "user-001", -1, 0, false, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void shouldThrowExceptionWhenAssistsAreNegative() {
        // when & then
        assertThatThrownBy(() -> new PlayerPerformance(
                "perf-001", "match-001", "club-001", "user-001", 0, -1, false, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    // Phase 2: MatchRecord Aggregate tests
    @Test
    void shouldAddPlayerPerformanceToMatchRecord() {
        // given
        final MatchRecord record = MatchRecord.create(
                "match-001", "club-001", null, LocalDate.now(),
                "상대팀FC", new MatchScore(2, 1), "user-001", "2025"
        );

        final PlayerPerformance performance = new PlayerPerformance(
                "perf-001", "match-001", "club-001", "user-002",
                1, 1, false, 90
        );

        // when
        record.addPlayerPerformance(performance);

        // then
        assertThat(record.getPlayerPerformances()).hasSize(1);
        assertThat(record.getPlayerPerformances().get(0)).isEqualTo(performance);
    }

    @Test
    void shouldPublishRecordedMatchEventWhenCreating() {
        // given
        final String id = "match-001";
        final String clubId = "club-001";
        final LocalDate matchDate = LocalDate.now();
        final MatchScore score = new MatchScore(2, 1);

        final MatchRecord record = MatchRecord.create(
                id, clubId, null, matchDate, "상대팀FC", score, "user-001", "2025"
        );

        final List<DomainEvent> capturedEvents = new ArrayList<>();

        // when
        record.publish(capturedEvents::add);

        // then
        assertThat(capturedEvents).hasSize(1);
        assertThat(capturedEvents.get(0)).isInstanceOf(RecordedMatchEvent.class);

        final RecordedMatchEvent event = (RecordedMatchEvent) capturedEvents.get(0);
        assertThat(event.matchRecordId()).isEqualTo(id);
        assertThat(event.clubId()).isEqualTo(clubId);
        assertThat(event.matchDate()).isEqualTo(matchDate);
        assertThat(event.result()).isEqualTo(MatchResult.WIN);
    }

    // Phase 1: MatchRecord.update() tests
    @Test
    void shouldUpdateMatchRecordScoreOpponentNameAndDate() {
        // given
        final MatchRecord record = MatchRecord.create(
                "match-001", "club-001", null, LocalDate.of(2025, 1, 1),
                "상대팀FC", new MatchScore(2, 1), "user-001", "2025"
        );

        final LocalDate newMatchDate = LocalDate.of(2025, 1, 15);
        final String newOpponentName = "새상대팀FC";
        final MatchScore newScore = new MatchScore(3, 2);

        // when
        final MatchRecord updated = record.update(newMatchDate, newOpponentName, newScore, List.of());

        // then
        assertThat(updated.getId()).isEqualTo(record.getId());
        assertThat(updated.getClubId()).isEqualTo(record.getClubId());
        assertThat(updated.getMatchDate()).isEqualTo(newMatchDate);
        assertThat(updated.getOpponentName()).isEqualTo(newOpponentName);
        assertThat(updated.getScore()).isEqualTo(newScore);
        assertThat(updated.getResult()).isEqualTo(MatchResult.WIN);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(record.getCreatedAt());
    }

    @Test
    void shouldUpdateResultWhenScoreChanges() {
        // given
        final MatchRecord record = MatchRecord.create(
                "match-001", "club-001", null, LocalDate.of(2025, 1, 1),
                "상대팀FC", new MatchScore(2, 1), "user-001", "2025"
        );

        // when (change from WIN to LOSE)
        final MatchRecord updated = record.update(
                record.getMatchDate(),
                record.getOpponentName(),
                new MatchScore(1, 3),
                List.of()
        );

        // then
        assertThat(updated.getResult()).isEqualTo(MatchResult.LOSE);
    }

    // Phase 1: MatchRecord.delete() tests
    @Test
    void shouldSetDeletedAtWhenDeleting() {
        // given
        final MatchRecord record = MatchRecord.create(
                "match-001", "club-001", null, LocalDate.now(),
                "상대팀FC", new MatchScore(2, 1), "user-001", "2025"
        );
        assertThat(record.getDeletedAt()).isNull();

        // when
        final MatchRecord deleted = record.delete();

        // then
        assertThat(deleted.getDeletedAt()).isNotNull();
        assertThat(deleted.getId()).isEqualTo(record.getId());
    }

    @Test
    void shouldPublishDeletedMatchEventWhenDeleting() {
        // given
        final MatchRecord record = MatchRecord.create(
                "match-001", "club-001", null, LocalDate.now(),
                "상대팀FC", new MatchScore(2, 1), "user-001", "2025"
        );
        // consume create event
        record.publish(e -> {});

        final MatchRecord deleted = record.delete();
        final List<DomainEvent> capturedEvents = new ArrayList<>();

        // when
        deleted.publish(capturedEvents::add);

        // then
        assertThat(capturedEvents).hasSize(1);
        assertThat(capturedEvents.get(0)).isInstanceOf(DeletedMatchEvent.class);

        final DeletedMatchEvent event = (DeletedMatchEvent) capturedEvents.get(0);
        assertThat(event.matchRecordId()).isEqualTo("match-001");
        assertThat(event.clubId()).isEqualTo("club-001");
        assertThat(event.season()).isEqualTo("2025");
    }

    @Test
    void shouldReturnTrueWhenRecordIsDeleted() {
        // given
        final MatchRecord record = MatchRecord.create(
                "match-001", "club-001", null, LocalDate.now(),
                "상대팀FC", new MatchScore(2, 1), "user-001", "2025"
        );

        // when
        final MatchRecord deleted = record.delete();

        // then
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(record.isDeleted()).isFalse();
    }

    // Phase 1: UpdatedMatchEvent tests
    @Test
    void shouldPublishUpdatedMatchEventWhenUpdating() {
        // given
        final MatchRecord record = MatchRecord.create(
                "match-001", "club-001", null, LocalDate.of(2025, 1, 1),
                "상대팀FC", new MatchScore(2, 1), "user-001", "2025"
        );
        // consume create event
        record.publish(e -> {});

        final MatchRecord updated = record.update(
                LocalDate.of(2025, 1, 15),
                "새상대팀FC",
                new MatchScore(3, 2),
                List.of()
        );

        final List<DomainEvent> capturedEvents = new ArrayList<>();

        // when
        updated.publish(capturedEvents::add);

        // then
        assertThat(capturedEvents).hasSize(1);
        assertThat(capturedEvents.get(0)).isInstanceOf(UpdatedMatchEvent.class);

        final UpdatedMatchEvent event = (UpdatedMatchEvent) capturedEvents.get(0);
        assertThat(event.matchRecordId()).isEqualTo("match-001");
        assertThat(event.clubId()).isEqualTo("club-001");
        assertThat(event.matchDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(event.opponentName()).isEqualTo("새상대팀FC");
        assertThat(event.result()).isEqualTo(MatchResult.WIN);
    }

    @Test
    void shouldReplacePlayerPerformancesWhenUpdating() {
        // given
        final MatchRecord record = MatchRecord.create(
                "match-001", "club-001", null, LocalDate.of(2025, 1, 1),
                "상대팀FC", new MatchScore(2, 1), "user-001", "2025"
        );

        final PlayerPerformance oldPerformance = new PlayerPerformance(
                "perf-001", "match-001", "club-001", "user-002",
                1, 0, false, 90
        );
        record.addPlayerPerformance(oldPerformance);

        final PlayerPerformance newPerformance1 = new PlayerPerformance(
                "perf-002", "match-001", "club-001", "user-003",
                2, 1, true, 90
        );
        final PlayerPerformance newPerformance2 = new PlayerPerformance(
                "perf-003", "match-001", "club-001", "user-004",
                0, 1, false, 45
        );
        final List<PlayerPerformance> newPerformances = List.of(newPerformance1, newPerformance2);

        // when
        final MatchRecord updated = record.update(
                record.getMatchDate(),
                record.getOpponentName(),
                record.getScore(),
                newPerformances
        );

        // then
        assertThat(updated.getPlayerPerformances()).hasSize(2);
        assertThat(updated.getPlayerPerformances()).containsExactly(newPerformance1, newPerformance2);
        assertThat(updated.getPlayerPerformances()).doesNotContain(oldPerformance);
    }
}
