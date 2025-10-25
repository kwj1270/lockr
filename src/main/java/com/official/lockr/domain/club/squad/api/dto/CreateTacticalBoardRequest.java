package com.official.lockr.domain.club.squad.api.dto;

import com.official.lockr.domain.club.common.Position;
import com.official.lockr.domain.club.squad.application.dto.CreateTacticalBoardCommand;
import com.official.lockr.domain.club.squad.domain.board.player.*;

import java.util.List;

public record CreateTacticalBoardRequest(
        String name
) {
    public CreateTacticalBoardCommand toCommand(final String userId, final String squadId) {
        return new CreateTacticalBoardCommand(userId, name, squadId);
    }

    public record FieldPlayerRequest(
            String playerId,
            String position,
            int locationX,
            int locationY,
            boolean isCaptain
    ) {
        public FieldPlayer toPlayer() {
            return new FieldPlayer(playerId, Position.valueOf(position), new Location(locationX, locationY), isCaptain);
        }
    }

    public record BenchPlayerRequest(
            String playerId,
            String position
    ) {
        public BenchPlayer toPlayer() {
            return new BenchPlayer(playerId, Position.valueOf(position));
        }
    }

    public record NoneSelectedPlayerRequest(
            String squadPlayerId,
            String position
    ) {
        public NoneSelectedPlayer toPlayer() {
            return new NoneSelectedPlayer(squadPlayerId, Position.valueOf(position));
        }
    }
}
