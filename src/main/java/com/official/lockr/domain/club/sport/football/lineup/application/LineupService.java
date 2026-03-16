package com.official.lockr.domain.club.sport.football.lineup.application;

import com.official.lockr.domain.club.club.domain.ClubRepository;
import com.official.lockr.domain.club.sport.football.lineup.application.command.AddLineupsCommand;
import com.official.lockr.domain.club.sport.football.lineup.application.command.AssignSlotCommand;
import com.official.lockr.domain.club.sport.football.lineup.application.command.RemoveSlotCommand;
import com.official.lockr.domain.club.sport.football.lineup.application.usecase.AddLineupUseCase;
import com.official.lockr.domain.club.sport.football.lineup.application.usecase.AssignSlotUseCase;
import com.official.lockr.domain.club.sport.football.lineup.application.usecase.ChangeFormationUseCase;
import com.official.lockr.domain.club.sport.football.lineup.application.usecase.DeleteLineupUseCase;
import com.official.lockr.domain.club.sport.football.lineup.application.usecase.RemoveSlotUseCase;
import com.official.lockr.domain.club.sport.football.lineup.domain.Lineup;
import com.official.lockr.domain.club.sport.football.lineup.domain.LineupRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class LineupService implements AddLineupUseCase, AssignSlotUseCase, ChangeFormationUseCase, RemoveSlotUseCase, DeleteLineupUseCase {

    private final ClubRepository clubRepository;
    private final LineupRepository lineUpRepository;

    public LineupService(final ClubRepository clubRepository,
                         final LineupRepository lineUpRepository
    ) {
        this.clubRepository = clubRepository;
        this.lineUpRepository = lineUpRepository;
    }

    @Override
    public List<Lineup> addAll(final AddLineupsCommand command) {
        final List<Lineup> clubs = lineUpRepository.findByClubId(command.clubId());
        if (!clubs.isEmpty()) {
            return clubs;
        }
        final List<Lineup> lineups = IntStream.range(0, 3)
                .mapToObj(it -> new Lineup(command.clubId(), "포메이션" + (char)('A' + it)))
                .toList();
        return lineUpRepository.saveAll(lineups);
    }

    @Override
    public Lineup assign(final AssignSlotCommand command) {
        final Lineup lineup = lineup(command.lineupId(), command.clubId());
        lineup.assignSlot(command.squadPlayerId(), command.slotType(), command.slotIndex());
        return lineUpRepository.save(lineup);
    }

    @Override
    public Lineup changeFormation(final String clubId, final String lineupId, final String formation) {
        final Lineup lineup = lineup(lineupId, clubId);
        lineup.changeFormation(formation);
        return lineUpRepository.save(lineup);
    }

    @Override
    public Lineup removeSlot(final RemoveSlotCommand command) {
        final Lineup lineup = lineup(command.lineupId(), command.clubId());
        lineup.removeSlot(command.slotType(), command.slotIndex());
        return lineUpRepository.save(lineup);
    }

    @Override
    public void delete(final String clubId, final String lineupId) {
        final Lineup lineup = lineup(lineupId, clubId);
        lineup.delete();
        lineUpRepository.delete(lineup);
    }

    @NonNull
    private Lineup lineup(final String lineupId, String clubId) {
        final Lineup lineup = lineUpRepository.findById(lineupId);
        if (lineup == null || !lineup.getClubId().equals(clubId)) {
            throw new IllegalArgumentException("Lineup not found: " + lineupId);
        }
        return lineup;
    }
}
