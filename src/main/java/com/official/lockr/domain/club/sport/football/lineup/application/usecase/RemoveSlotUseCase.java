package com.official.lockr.domain.club.sport.football.lineup.application.usecase;

import com.official.lockr.domain.club.sport.football.lineup.application.command.RemoveSlotCommand;
import com.official.lockr.domain.club.sport.football.lineup.domain.Lineup;

public interface RemoveSlotUseCase {

    default Lineup removeSlot(final String clubId, final String lineupId, final String slotType, final int slotIndex) {
        final RemoveSlotCommand command = new RemoveSlotCommand(clubId, lineupId, slotType, slotIndex);
        return removeSlot(command);
    }

    Lineup removeSlot(RemoveSlotCommand command);
}
