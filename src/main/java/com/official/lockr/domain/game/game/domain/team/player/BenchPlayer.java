package com.official.lockr.domain.game.game.domain.team.player;

import com.official.lockr.domain.game.common.Cards;
import com.official.lockr.domain.game.common.Position;

import java.util.Objects;

public class BenchPlayer extends GamePlayer {

    private final Position position;

    public BenchPlayer(final String playerId,
                       final String playerName,
                       final int backNumber,
                       final Cards cards,
                       final Position position) {
        super(playerId, playerName, backNumber, cards);
        this.position = Objects.requireNonNull(position, "Substitute player must have position");
    }

    @Override
    public GamePlayerType getEntryType() {
        return GamePlayerType.BENCH;
    }

    public Position getPosition() {
        return position;
    }

    public static BenchPlayer fromField(final FieldPlayer startingPlayer) {
        return new BenchPlayer(
                startingPlayer.getPlayerId(),
                startingPlayer.getPlayerName(),
                startingPlayer.getBackNumber(),
                startingPlayer.getCards(),
                startingPlayer.getPosition()
        );
    }
}
