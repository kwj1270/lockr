package com.official.lockr.domain.club.stats.application;

import com.official.lockr.domain.club.stats.application.command.PlayerPerformanceCommand;
import com.official.lockr.domain.club.stats.application.command.RecordMatchCommand;
import com.official.lockr.domain.club.stats.application.command.UpdateMatchCommand;
import com.official.lockr.domain.club.stats.domain.MatchRecord;
import com.official.lockr.domain.club.stats.domain.MatchRecordRepository;
import com.official.lockr.domain.club.stats.domain.MatchScore;
import com.official.lockr.domain.club.stats.domain.StatsClub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private MatchRecordRepository matchRecordRepository;

    @Mock
    private StatsClub statsClub;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(matchRecordRepository, statsClub);
    }

    // Phase 3: record() authorization tests
    @Test
    void shouldThrowExceptionWhenNonStaffTriesToRecordMatch() {
        // given
        final String clubId = "club-001";
        final String userId = "user-001";

        doThrow(new IllegalArgumentException()).when(statsClub).verifyStaffMembership(userId, clubId);

        final RecordMatchCommand command = new RecordMatchCommand(
                clubId, null, LocalDate.now(), "상대팀FC", 2, 1,
                userId, "2025", List.of()
        );

        // when & then
        assertThatThrownBy(() -> statsService.record(command))
                .isInstanceOf(IllegalArgumentException.class);

        verify(matchRecordRepository, never()).save(any());
    }

    @Test
    void shouldRecordMatchWhenUserIsCoach() {
        // given
        final String clubId = "club-001";
        final String userId = "user-001";

        given(matchRecordRepository.save(any(MatchRecord.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        final RecordMatchCommand command = new RecordMatchCommand(
                clubId, null, LocalDate.now(), "상대팀FC", 2, 1,
                userId, "2025", List.of()
        );

        // when
        final MatchRecord recorded = statsService.record(command);

        // then
        assertThat(recorded).isNotNull();
        assertThat(recorded.getClubId()).isEqualTo(clubId);
        verify(matchRecordRepository).save(any(MatchRecord.class));
    }

    @Test
    void shouldThrowExceptionWhenClubNotFoundOnRecord() {
        // given
        final String clubId = "non-existent-club";
        final String userId = "user-001";

        doThrow(new IllegalArgumentException()).when(statsClub).verifyStaffMembership(userId, clubId);

        final RecordMatchCommand command = new RecordMatchCommand(
                clubId, null, LocalDate.now(), "상대팀FC", 2, 1,
                userId, "2025", List.of()
        );

        // when & then
        assertThatThrownBy(() -> statsService.record(command))
                .isInstanceOf(IllegalArgumentException.class);

        verify(matchRecordRepository, never()).save(any());
    }

    // Phase 3: update() tests
    @Test
    void shouldThrowExceptionWhenNonStaffTriesToUpdateMatch() {
        // given
        final String clubId = "club-001";
        final String recordId = "match-001";
        final String userId = "user-001";

        doThrow(new IllegalArgumentException()).when(statsClub).verifyStaffMembership(userId, clubId);

        final UpdateMatchCommand command = new UpdateMatchCommand(
                recordId, clubId, userId,
                LocalDate.now(), "상대팀FC", 2, 1, List.of()
        );

        // when & then
        assertThatThrownBy(() -> statsService.update(command))
                .isInstanceOf(IllegalArgumentException.class);

        verify(matchRecordRepository, never()).save(any());
    }

    @Test
    void shouldUpdateMatchWhenUserIsCoach() {
        // given
        final String clubId = "club-001";
        final String recordId = "match-001";
        final String userId = "user-001";

        final MatchRecord existingRecord = createMatchRecord(recordId, clubId);
        given(matchRecordRepository.findById(recordId)).willReturn(existingRecord);
        given(matchRecordRepository.save(any(MatchRecord.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        final LocalDate newDate = LocalDate.of(2025, 2, 1);
        final UpdateMatchCommand command = new UpdateMatchCommand(
                recordId, clubId, userId,
                newDate, "새상대팀FC", 3, 2, List.of()
        );

        // when
        final MatchRecord updated = statsService.update(command);

        // then
        assertThat(updated.getMatchDate()).isEqualTo(newDate);
        assertThat(updated.getOpponentName()).isEqualTo("새상대팀FC");
        assertThat(updated.getScore().ourScore()).isEqualTo(3);
        assertThat(updated.getScore().opponentScore()).isEqualTo(2);

        verify(matchRecordRepository).save(any(MatchRecord.class));
    }

    @Test
    void shouldUpdateMatchWhenUserIsManager() {
        // given
        final String clubId = "club-001";
        final String recordId = "match-001";
        final String userId = "user-001";

        final MatchRecord existingRecord = createMatchRecord(recordId, clubId);
        given(matchRecordRepository.findById(recordId)).willReturn(existingRecord);
        given(matchRecordRepository.save(any(MatchRecord.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        final UpdateMatchCommand command = new UpdateMatchCommand(
                recordId, clubId, userId,
                LocalDate.now(), "상대팀FC", 2, 1, List.of()
        );

        // when
        final MatchRecord updated = statsService.update(command);

        // then
        assertThat(updated).isNotNull();
        verify(matchRecordRepository).save(any(MatchRecord.class));
    }

    // Phase 3: delete() tests
    @Test
    void shouldThrowExceptionWhenNonStaffTriesToDeleteMatch() {
        // given
        final String clubId = "club-001";
        final String recordId = "match-001";
        final String userId = "user-001";

        doThrow(new IllegalArgumentException()).when(statsClub).verifyStaffMembership(userId, clubId);

        // when & then
        assertThatThrownBy(() -> statsService.delete(clubId, recordId, userId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(matchRecordRepository, never()).delete(any());
    }

    @Test
    void shouldDeleteMatchWhenUserIsStaff() {
        // given
        final String clubId = "club-001";
        final String recordId = "match-001";
        final String userId = "user-001";

        final MatchRecord existingRecord = createMatchRecord(recordId, clubId);
        given(matchRecordRepository.findById(recordId)).willReturn(existingRecord);

        // when
        statsService.delete(clubId, recordId, userId);

        // then
        verify(matchRecordRepository).delete(existingRecord);
    }

    @Test
    void shouldThrowExceptionWhenClubNotFound() {
        // given
        final String clubId = "non-existent-club";
        final String recordId = "match-001";
        final String userId = "user-001";

        doThrow(new IllegalArgumentException()).when(statsClub).verifyStaffMembership(userId, clubId);

        // when & then
        assertThatThrownBy(() -> statsService.delete(clubId, recordId, userId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // clubId ownership validation tests
    @Test
    void shouldThrowExceptionWhenUpdateRecordBelongsToDifferentClub() {
        // given
        final String clubId = "club-001";
        final String otherClubId = "club-002";
        final String recordId = "match-001";
        final String userId = "user-001";

        final MatchRecord existingRecord = createMatchRecord(recordId, otherClubId);
        given(matchRecordRepository.findById(recordId)).willReturn(existingRecord);

        final UpdateMatchCommand command = new UpdateMatchCommand(
                recordId, clubId, userId,
                LocalDate.now(), "상대팀FC", 2, 1, List.of()
        );

        // when & then
        assertThatThrownBy(() -> statsService.update(command))
                .isInstanceOf(IllegalArgumentException.class);

        verify(matchRecordRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenDeleteRecordBelongsToDifferentClub() {
        // given
        final String clubId = "club-001";
        final String otherClubId = "club-002";
        final String recordId = "match-001";
        final String userId = "user-001";

        final MatchRecord existingRecord = createMatchRecord(recordId, otherClubId);
        given(matchRecordRepository.findById(recordId)).willReturn(existingRecord);

        // when & then
        assertThatThrownBy(() -> statsService.delete(clubId, recordId, userId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(matchRecordRepository, never()).delete(any());
    }

    @Test
    void shouldUpdateMatchWithPlayerPerformances() {
        // given
        final String clubId = "club-001";
        final String recordId = "match-001";
        final String userId = "user-001";

        final MatchRecord existingRecord = createMatchRecord(recordId, clubId);
        given(matchRecordRepository.findById(recordId)).willReturn(existingRecord);
        given(matchRecordRepository.save(any(MatchRecord.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        final List<PlayerPerformanceCommand> playerPerformances = List.of(
                new PlayerPerformanceCommand("user-002", 2, 1, true, 90),
                new PlayerPerformanceCommand("user-003", 1, 0, false, 45)
        );

        final UpdateMatchCommand command = new UpdateMatchCommand(
                recordId, clubId, userId,
                LocalDate.now(), "상대팀FC", 3, 1, playerPerformances
        );

        // when
        final MatchRecord updated = statsService.update(command);

        // then
        assertThat(updated.getPlayerPerformances()).hasSize(2);
        assertThat(updated.getPlayerPerformances().get(0).getUserId()).isEqualTo("user-002");
        assertThat(updated.getPlayerPerformances().get(0).getGoals()).isEqualTo(2);
        assertThat(updated.getPlayerPerformances().get(0).getAssists()).isEqualTo(1);
        assertThat(updated.getPlayerPerformances().get(0).isMom()).isTrue();
        assertThat(updated.getPlayerPerformances().get(1).getUserId()).isEqualTo("user-003");
        assertThat(updated.getPlayerPerformances().get(1).getGoals()).isEqualTo(1);
        assertThat(updated.getPlayerPerformances().get(1).isMom()).isFalse();

        verify(matchRecordRepository).save(any(MatchRecord.class));
    }

    private MatchRecord createMatchRecord(final String recordId, final String clubId) {
        return MatchRecord.create(
                recordId, clubId, null, LocalDate.now(),
                "상대팀FC", new MatchScore(2, 1), "user-001", "2025"
        );
    }
}
