package com.official.lockr.domain.club.squad.application;

import com.official.lockr.domain.club.squad.application.dto.AddMatchPlayerTacticalBoardCommand;
import com.official.lockr.domain.club.squad.application.dto.ApplyFormationTacticalBoardCommand;
import com.official.lockr.domain.club.squad.application.dto.CreateTacticalBoardCommand;
import com.official.lockr.domain.club.squad.application.dto.MoveLocationTacticalBoardCommand;
import com.official.lockr.domain.club.squad.application.dto.SubstitutePlayerCommand;
import com.official.lockr.domain.club.squad.application.usecase.*;
import com.official.lockr.domain.club.squad.domain.board.Formation;
import com.official.lockr.domain.club.squad.domain.board.Staff;
import com.official.lockr.domain.club.squad.domain.board.Staffs;
import com.official.lockr.domain.club.squad.domain.board.TacticalBoard;
import com.official.lockr.domain.club.squad.domain.board.TacticalBoardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class TacticalBoardService implements
        CreateTacticalBoardUseCase,
        AddMatchPlayerTacticalBoardUseCase,
        MovePositionTacticalBoardUseCase,
        SubstituteMatchPlayerUseCase,
        DeleteTacticalBoardUseCase,
        ApplyFormationTacticalBoardUseCase
{

    private final Staffs staffs;
    private final TacticalBoardRepository tacticalBoardRepository;

    public TacticalBoardService(final Staffs staffs,
                                final TacticalBoardRepository tacticalBoardRepository
    ) {
        this.staffs = staffs;
        this.tacticalBoardRepository = tacticalBoardRepository;
    }

    @Override
    public TacticalBoard create(final CreateTacticalBoardCommand command) {
        final Staff staff = staff(command.squadId(), command.staffUserId());
        final List<TacticalBoard> tacticalBoards = tacticalBoardRepository.findAllBySquadId(command.squadId());
        if(tacticalBoards.size() >= 3) {
            throw new IllegalArgumentException();
        }
        final TacticalBoard tacticalBoard = TacticalBoard.init(generateUlid(), command.squadId(), staff.userId(), command.name());
        return tacticalBoardRepository.save(tacticalBoard);
    }

    @Override
    public TacticalBoard addMatchPlayer(final AddMatchPlayerTacticalBoardCommand command) {
        final Staff staff = staff(command.squadId(), command.staffUserId());
        final TacticalBoard tacticalBoard = tacticalBoardRepository.findById(command.tacticalBoardId());
        if(isNull(tacticalBoard)) {
            throw new IllegalArgumentException();
        }
        tacticalBoard.addMatchPlayer(staff.userId(), command.squadPlayerId(), command.tacticalBoardPlayerType(), command.x(), command.y());
        return tacticalBoardRepository.save(tacticalBoard);
    }

    @Override
    public TacticalBoard moveLocation(final MoveLocationTacticalBoardCommand command) {
        final Staff staff = staff(command.squadId(), command.staffUserId());
        final TacticalBoard tacticalBoard = tacticalBoardRepository.findById(command.tacticalBoardId());
        if (isNull(tacticalBoard)) {
            throw new IllegalArgumentException();
        }
        tacticalBoard.moveLocation(staff.userId(), command.squadPlayerId(), command.x(), command.y());
        return tacticalBoardRepository.save(tacticalBoard);
    }

    @Override
    public TacticalBoard substitute(final SubstitutePlayerCommand command) {
        final Staff staff = staff(command.squadId(), command.staffUserId());
        final TacticalBoard tacticalBoard = tacticalBoardRepository.findById(command.tacticalBoardId());
        if (isNull(tacticalBoard)) {
            throw new IllegalArgumentException("Entry not found: " + command.tacticalBoardId());
        }
        tacticalBoard.substitute(staff.userId(), command.outPlayerId(), command.outPlayerType(), command.inPlayerId(), command.inPlayerType());
        return tacticalBoardRepository.save(tacticalBoard);
    }

    @Override
    public void delete(final String squadId, final String staffUserId, final String tacticalBoardId) {
        staff(squadId, staffUserId);
        tacticalBoardRepository.delete(tacticalBoardId);
    }

    @Override
    public TacticalBoard applyFormation(final ApplyFormationTacticalBoardCommand command) {
        final Staff staff = staff(command.squadId(), command.staffUserId());
        final TacticalBoard tacticalBoard = tacticalBoardRepository.findById(command.tacticalBoardId());
        if (isNull(tacticalBoard)) {
            throw new IllegalArgumentException("TacticalBoard not found: " + command.tacticalBoardId());
        }
        tacticalBoard.applyFormation(staff.userId(), Formation.fromName(command.formationName()));
        return tacticalBoardRepository.save(tacticalBoard);
    }

    private Staff staff(final String squadId, final String userId) {
        final Staff staff = staffs.find(squadId, userId);
        if (isNull(staff)) {
            throw new IllegalArgumentException();
        }
        return staff;
    }
}
