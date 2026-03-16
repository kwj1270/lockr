package com.official.lockr.domain.game.game.application.command;

import com.official.lockr.domain.game.game.domain.team.player.Bench;
import com.official.lockr.domain.game.game.domain.team.player.Field;

public record RegisterEntryGameCommand(
        String gameId,
        String teamId,
        String userId,
        Field fieldPlayers,
        Bench benchPlayers
) {
}
