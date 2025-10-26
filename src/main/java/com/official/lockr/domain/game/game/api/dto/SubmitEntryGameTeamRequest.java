package com.official.lockr.domain.game.game.api.dto;

import com.official.lockr.domain.game.game.application.command.RegisterEntryGameCommand;
import com.official.lockr.domain.game.game.domain.team.player.BenchPlayers;
import com.official.lockr.domain.game.game.domain.team.player.FieldPlayers;

public record SubmitEntryGameTeamRequest(
        FieldPlayers fieldPlayers,
        BenchPlayers benchPlayers
) {
    public RegisterEntryGameCommand toCommand(final String gameId, final String teamId, final String userId) {
        return new RegisterEntryGameCommand(gameId, teamId, userId, fieldPlayers, benchPlayers);
    }
}
