package com.official.lockr.domain.game.game.domain.team.player;

import com.official.lockr.domain.game.common.RedCard;
import com.official.lockr.domain.game.common.YellowCard;

import java.util.List;

import static java.util.Objects.isNull;

public class Lineup {

    private final FieldPlayers fieldPlayers;
    private final BenchPlayers benchPlayers;

    public Lineup(final List<GamePlayer> gamePlayers) {
        this(FieldPlayers.of(gamePlayers), BenchPlayers.of(gamePlayers));
    }

    public Lineup(final FieldPlayers fieldPlayers, final BenchPlayers benchPlayers) {
        this.fieldPlayers = fieldPlayers;
        this.benchPlayers = benchPlayers;
    }

    public void substitute(final String outPlayerId, final String inPlayerId, final int minute) {
        final FieldPlayer outPlayer = fieldPlayers.findPlayerById(outPlayerId);
        final BenchPlayer inPlayer = benchPlayers.findPlayerById(inPlayerId);
        if (isNull(outPlayer) || isNull(inPlayer)) {
            throw new IllegalArgumentException();
        }
        final FieldPlayer filedPlayer = FieldPlayer.fromBench(inPlayer, outPlayer.getPosition(), outPlayer.getLocation());
        final BenchPlayer benchPlayer = BenchPlayer.fromField(outPlayer);
        if (outPlayer.isCaptain()) {
            outPlayer.releaseCaptain();
            fieldPlayers.assignCaptainToStartingGK();
        }
        fieldPlayers.remove(outPlayer);
        benchPlayers.remove(inPlayer);
        fieldPlayers.add(filedPlayer);
        benchPlayers.add(benchPlayer);
    }

    public void yellowCard(final String playerId, final int minute) {
        final GamePlayer gamePlayer = findFieldPlayer(playerId);
        gamePlayer.addCard(new YellowCard(minute));
    }

    public void redCard(final String playerId, final int minute) {
        final FieldPlayer player = fieldPlayers.findPlayerById(playerId);
        if (isNull(player)) {
            throw new IllegalArgumentException();
        }
        player.addCard(new RedCard(minute));
        fieldPlayers.remove(player);
    }

    public boolean hasPlayerToBeSentOff(final String playerId) {
        final GamePlayer gamePlayer = findFieldPlayer(playerId);
        if (gamePlayer == null) {
            throw new IllegalArgumentException();
        }
        return gamePlayer.hasTwoYellowCard();
    }

    private GamePlayer findFieldPlayer(final String playerId) {
        final FieldPlayer player = fieldPlayers.findPlayerById(playerId);
        if (isNull(player)) {
            throw new IllegalArgumentException();
        }
        return player;
    }

    public boolean isRegistered() {
        return fieldPlayers.isRegistered() && benchPlayers.isRegistered();
    }
}
