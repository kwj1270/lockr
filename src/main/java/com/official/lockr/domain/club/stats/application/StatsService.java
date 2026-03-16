package com.official.lockr.domain.club.stats.application;

import com.official.lockr.domain.club.stats.application.command.PlayerPerformanceCommand;
import com.official.lockr.domain.club.stats.application.command.RecordMatchCommand;
import com.official.lockr.domain.club.stats.application.command.UpdateMatchCommand;
import com.official.lockr.domain.club.stats.application.usecase.DeleteMatchUseCase;
import com.official.lockr.domain.club.stats.application.usecase.RecordMatchUseCase;
import com.official.lockr.domain.club.stats.application.usecase.UpdateMatchUseCase;
import com.official.lockr.domain.club.stats.domain.MatchRecord;
import com.official.lockr.domain.club.stats.domain.MatchRecordRepository;
import com.official.lockr.domain.club.stats.domain.MatchScore;
import com.official.lockr.domain.club.stats.domain.PlayerPerformance;
import com.official.lockr.domain.club.stats.domain.StatsClub;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class StatsService implements RecordMatchUseCase, UpdateMatchUseCase, DeleteMatchUseCase {

    private final MatchRecordRepository matchRecordRepository;
    private final StatsClub statsClub;

    public StatsService(final MatchRecordRepository matchRecordRepository,
                        final StatsClub statsClub) {
        this.matchRecordRepository = matchRecordRepository;
        this.statsClub = statsClub;
    }

    @Override
    public MatchRecord record(final RecordMatchCommand command) {
        statsClub.verifyStaffMembership(command.recordedBy(), command.clubId());

        if (command.scheduleId() != null) {
            final MatchRecord existingRecord = matchRecordRepository.findByScheduleId(command.scheduleId());
            if (existingRecord != null) {
                throw new IllegalArgumentException("Match record already exists for this schedule");
            }
        }

        final MatchRecord matchRecord = MatchRecord.create(
                generateUlid(),
                command.clubId(),
                command.scheduleId(),
                command.matchDate(),
                command.opponentName(),
                new MatchScore(command.ourScore(), command.opponentScore()),
                command.recordedBy(),
                command.season()
        );

        for (final PlayerPerformanceCommand perfCommand : command.playerPerformances()) {
            final PlayerPerformance performance = new PlayerPerformance(
                    generateUlid(),
                    matchRecord.getId(),
                    command.clubId(),
                    perfCommand.userId(),
                    perfCommand.goals(),
                    perfCommand.assists(),
                    perfCommand.isMom(),
                    perfCommand.minutesPlayed()
            );
            matchRecord.addPlayerPerformance(performance);
        }

        return matchRecordRepository.save(matchRecord);
    }

    @Override
    public MatchRecord update(final UpdateMatchCommand command) {
        statsClub.verifyStaffMembership(command.userId(), command.clubId());

        final MatchRecord existingRecord = matchRecordRepository.findById(command.recordId());
        if (isNull(existingRecord)) {
            throw new IllegalArgumentException("Match record not found");
        }
        if (!existingRecord.getClubId().equals(command.clubId())) {
            throw new IllegalArgumentException("Match record does not belong to the club");
        }

        final List<PlayerPerformance> newPlayerPerformances = command.playerPerformances().stream()
                .map(perfCommand -> new PlayerPerformance(
                        generateUlid(),
                        command.recordId(),
                        command.clubId(),
                        perfCommand.userId(),
                        perfCommand.goals(),
                        perfCommand.assists(),
                        perfCommand.isMom(),
                        perfCommand.minutesPlayed()
                ))
                .toList();

        final MatchRecord updatedRecord = existingRecord.update(
                command.matchDate(),
                command.opponentName(),
                new MatchScore(command.ourScore(), command.opponentScore()),
                newPlayerPerformances
        );

        return matchRecordRepository.save(updatedRecord);
    }

    @Override
    public void delete(final String clubId, final String recordId, final String userId) {
        statsClub.verifyStaffMembership(userId, clubId);

        final MatchRecord existingRecord = matchRecordRepository.findById(recordId);
        if (isNull(existingRecord)) {
            throw new IllegalArgumentException("Match record not found");
        }
        if (!existingRecord.getClubId().equals(clubId)) {
            throw new IllegalArgumentException("Match record does not belong to the club");
        }

        matchRecordRepository.delete(existingRecord);
    }
}
