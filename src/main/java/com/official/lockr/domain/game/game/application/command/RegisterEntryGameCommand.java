package com.official.lockr.domain.game.game.application.command;

import com.official.lockr.domain.game.game.domain.team.player.BenchPlayers;
import com.official.lockr.domain.game.game.domain.team.player.FieldPlayers;

public record RegisterEntryGameCommand(
        String gameId,
        String teamId,
        String userId,
        FieldPlayers fieldPlayers,
        BenchPlayers benchPlayers
) {
}
