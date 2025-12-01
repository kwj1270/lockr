package com.official.lockr.domain.club.lineup.tacticalboard.application;

import com.official.lockr.domain.club.lineup.tacticalboard.application.dto.*;
import com.official.lockr.domain.club.lineup.tacticalboard.application.usecase.*;
import com.official.lockr.domain.club.lineup.tacticalboard.domain.*;
import com.official.lockr.global.vo.Formation;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class TacticalBoardService implements
        CreateTacticalBoardUseCase,
        AddPlayerTacticalBoardUseCase,
        MoveLocationTacticalBoardUseCase,
        SubstitutePlayerUseCase,
        DeleteTacticalBoardUseCase,
        ApplyFormationTacticalBoardUseCase {

    private final CoachingStaff coachingStaff;
    private final SquadPlayers squadPlayers;
    private final TacticalBoardRepository tacticalBoardRepository;

    public TacticalBoardService(final CoachingStaff coachingStaff,
                                final SquadPlayers squadPlayers,
                                final TacticalBoardRepository tacticalBoardRepository
    ) {
        this.coachingStaff = coachingStaff;
        this.squadPlayers = squadPlayers;
        this.tacticalBoardRepository = tacticalBoardRepository;
    }

    @Override
    public TacticalBoard create(final CreateTacticalBoardCommand command) {
        final Coach coach = staff(command.clubId(), command.coachUserId());
        final List<TacticalBoard> tacticalBoards = tacticalBoardRepository.findAllByClubId(command.clubId());
        if (tacticalBoards.size() >= 3) {
            throw new IllegalArgumentException();
        }
        final TacticalBoard tacticalBoard = TacticalBoard.init(generateUlid(), command.clubId(), coach.userId(), command.name());
        return tacticalBoardRepository.save(tacticalBoard);
    }

    @Override
    public TacticalBoard addPlayer(final AddPlayerTacticalBoardCommand command) {
        final Coach coach = staff(command.clubId(), command.coachUserId());
        final SquadPlayer squadPlayer = squadPlayer(command.playerId());
        final TacticalBoard tacticalBoard = tacticalBoard(command.tacticalBoardId());
        tacticalBoard.addPlayer(coach.userId(), squadPlayer.id(), command.playerType(), command.x(), command.y());
        return tacticalBoardRepository.save(tacticalBoard);
    }

    @Override
    public TacticalBoard moveLocation(final MoveLocationTacticalBoardCommand command) {
        final Coach coach = staff(command.clubId(), command.coachUserId());
        final TacticalBoard tacticalBoard = tacticalBoard(command.tacticalBoardId());
        tacticalBoard.moveLocation(coach.userId(), command.playerId(), command.x(), command.y());
        return tacticalBoardRepository.save(tacticalBoard);
    }

    @Override
    public TacticalBoard substitute(final SubstitutePlayerCommand command) {
        final Coach coach = staff(command.clubId(), command.coachUserId());
        final TacticalBoard tacticalBoard = tacticalBoard(command.tacticalBoardId());
        tacticalBoard.substitute(coach.userId(), command.outPlayerId(), command.outPlayerType(), command.inPlayerId(), command.inPlayerType());
        return tacticalBoardRepository.save(tacticalBoard);
    }

    @Override
    public void delete(final String clubId, final String coachUserId, final String tacticalBoardId) {
        staff(clubId, coachUserId);
        tacticalBoardRepository.delete(tacticalBoardId);
    }

    @Override
    public TacticalBoard applyFormation(final ApplyFormationTacticalBoardCommand command) {
        final Coach coach = staff(command.clubId(), command.coachUserId());
        final TacticalBoard tacticalBoard = tacticalBoardRepository.findById(command.tacticalBoardId());
        if (isNull(tacticalBoard)) {
            throw new IllegalArgumentException("TacticalBoard not found: " + command.tacticalBoardId());
        }
        tacticalBoard.applyFormation(coach.userId(), Formation.fromName(command.formationName()));
        return tacticalBoardRepository.save(tacticalBoard);
    }

    private Coach staff(final String clubId, final String userId) {
        final Coach coach = coachingStaff.find(clubId, userId);
        if (isNull(coach)) {
            throw new IllegalArgumentException();
        }
        return coach;
    }

    private SquadPlayer squadPlayer(final String playerId) {
        final SquadPlayer squadPlayer = squadPlayers.findByPlayerId(playerId);
        if (isNull(squadPlayer)) {
            throw new IllegalArgumentException();
        }
        return squadPlayer;
    }

    private TacticalBoard tacticalBoard(final String tacticalBoardId) {
        final TacticalBoard tacticalBoard = tacticalBoardRepository.findById(tacticalBoardId);
        if (isNull(tacticalBoard)) {
            throw new IllegalArgumentException();
        }
        return tacticalBoard;
    }
}
