package com.official.lockr.domain.club.sport.football.lineup.application.usecase;

import com.official.lockr.domain.club.sport.football.lineup.application.command.AssignSlotCommand;
import com.official.lockr.domain.club.sport.football.lineup.domain.Lineup;

public interface AssignSlotUseCase {

    default Lineup assign(final String clubId, final String lineupId, final String slotType, final int slotIndex, final String squadPlayerId) {
        final AssignSlotCommand command = new AssignSlotCommand(clubId, lineupId, slotType, slotIndex, squadPlayerId);
        return assign(command);
    }

    Lineup assign(AssignSlotCommand command);
}
