package com.official.lockr.domain.game.game.domain.team.player;

import com.official.lockr.domain.game.common.RedCard;
import com.official.lockr.domain.game.common.YellowCard;

import java.util.List;

import static java.util.Objects.isNull;

public class Entry {

    private final Field field;
    private final Bench bench;

    public Entry(final List<Player> players) {
        this(Field.of(players), Bench.of(players));
    }

    public Entry(final Field field, final Bench bench) {
        this.field = field;
        this.bench = bench;
    }

    public void substitute(final String outPlayerId, final String inPlayerId, final int minute) {
        final FieldPlayer outPlayer = field.findPlayerById(outPlayerId);
        final BenchPlayer inPlayer = bench.findPlayerById(inPlayerId);
        if (isNull(outPlayer) || isNull(inPlayer)) {
            throw new IllegalArgumentException();
        }
        final FieldPlayer filedPlayer = FieldPlayer.fromBench(inPlayer, outPlayer.getPosition(), outPlayer.getLocation());
        final BenchPlayer benchPlayer = BenchPlayer.fromField(outPlayer);
        if (outPlayer.isCaptain()) {
            outPlayer.releaseCaptain();
            field.assignCaptainToStartingGK();
        }
        field.remove(outPlayer);
        bench.remove(inPlayer);
        field.add(filedPlayer);
        bench.add(benchPlayer);
    }

    public void yellowCard(final String playerId, final int minute) {
        final Player player = findFieldPlayer(playerId);
        player.addCard(new YellowCard(minute));
    }

    public void redCard(final String playerId, final int minute) {
        final FieldPlayer player = field.findPlayerById(playerId);
        if (isNull(player)) {
            throw new IllegalArgumentException();
        }
        player.addCard(new RedCard(minute));
        field.remove(player);
    }

    public boolean hasPlayerToBeSentOff(final String playerId) {
        final Player player = findFieldPlayer(playerId);
        if (player == null) {
            throw new IllegalArgumentException();
        }
        return player.hasTwoYellowCard();
    }

    private Player findFieldPlayer(final String playerId) {
        final FieldPlayer player = field.findPlayerById(playerId);
        if (isNull(player)) {
            throw new IllegalArgumentException();
        }
        return player;
    }

    public boolean isRegistered() {
        return field.isRegistered() && bench.isRegistered();
    }
}
