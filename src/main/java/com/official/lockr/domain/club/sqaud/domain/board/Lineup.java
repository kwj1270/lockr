package com.official.lockr.domain.club.sqaud.domain.board;

import com.official.lockr.domain.club.common.Position;
import com.official.lockr.domain.club.sqaud.domain.board.player.*;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;

public class Lineup {

    private final FieldPlayers fieldPlayers;
    private final BenchPlayers benchPlayers;
    private final NoneSelectedPlayers noneSelectedPlayers;
    private Formation formation;

    public Lineup() {
        this(new FieldPlayers(), new BenchPlayers(), new NoneSelectedPlayers(), Formation.FORMATION_4_3_3);
    }

    public Lineup(final FieldPlayers fieldPlayers,
                  final BenchPlayers benchPlayers,
                  final NoneSelectedPlayers noneSelectedPlayers,
                  final Formation formation
    ) {
        this.fieldPlayers = fieldPlayers;
        this.benchPlayers = benchPlayers;
        this.noneSelectedPlayers = noneSelectedPlayers;
        this.formation = formation;
    }

    public void addMatchPlayer(final String squadPlayerId,
                               final String tacticalBoardPlayerType,
                               @Nullable final Integer x,
                               @Nullable final Integer y
    ) {
        final TacticalBoardPlayerType playerType = TacticalBoardPlayerType.valueOf(tacticalBoardPlayerType);
        switch (playerType) {
            case FIELD -> fieldPlayers.addMatchPlayer(squadPlayerId, x, y);
            case BENCH -> benchPlayers.addMatchPlayer(squadPlayerId);
            case NONE_SELECTED -> noneSelectedPlayers.addMatchPlayer(squadPlayerId);
        }
    }

    public void substitute(final String outPlayerId, final String outPlayerType, final String inPlayerId, final String inPlayerType) {
        final TacticalBoardPlayerType outTacticalBoardPlayerType = TacticalBoardPlayerType.valueOf(outPlayerType);
        final TacticalBoardPlayerType inTacticalBoardPlayerType = TacticalBoardPlayerType.valueOf(inPlayerType);

        if (TacticalBoardPlayerType.FIELD.equals(outTacticalBoardPlayerType) && TacticalBoardPlayerType.BENCH.equals(inTacticalBoardPlayerType)) {
            final FieldPlayer fieldPlayer = fieldPlayers.findPlayerById(outPlayerId);
            final BenchPlayer benchPlayer = benchPlayers.findPlayerById(inPlayerId);

            if (fieldPlayer.isCaptain()) {
                fieldPlayer.releaseCaptain();
                fieldPlayers.assignCaptainToStartingGK();
            }

            final FieldPlayer newFieldPlayer = new FieldPlayer(benchPlayer.getSquadPlayerId(), fieldPlayer.getPosition(), fieldPlayer.getLocation(), false);
            final BenchPlayer newBenchPlayer = new BenchPlayer(fieldPlayer.getSquadPlayerId(), fieldPlayer.getPosition());

            fieldPlayers.remove(fieldPlayer);
            benchPlayers.remove(benchPlayer);
            fieldPlayers.add(newFieldPlayer);
            benchPlayers.add(newBenchPlayer);
        }

        if (TacticalBoardPlayerType.FIELD.equals(outTacticalBoardPlayerType) && TacticalBoardPlayerType.NONE_SELECTED.equals(inTacticalBoardPlayerType)) {
            final FieldPlayer fieldPlayer = fieldPlayers.findPlayerById(outPlayerId);
            final NoneSelectedPlayer noneSelectedPlayer = noneSelectedPlayers.findPlayerById(inPlayerId);

            if (fieldPlayer.isCaptain()) {
                fieldPlayer.releaseCaptain();
                fieldPlayers.assignCaptainToStartingGK();
            }

            final FieldPlayer newFieldPlayer = new FieldPlayer(noneSelectedPlayer.getSquadPlayerId(), fieldPlayer.getPosition(), fieldPlayer.getLocation(), false);
            final NoneSelectedPlayer newNoneSelectedPlayer = new NoneSelectedPlayer(fieldPlayer.getSquadPlayerId(), fieldPlayer.getPosition());

            fieldPlayers.remove(fieldPlayer);
            noneSelectedPlayers.remove(noneSelectedPlayer);
            fieldPlayers.add(newFieldPlayer);
            noneSelectedPlayers.add(newNoneSelectedPlayer);
        }

        if (TacticalBoardPlayerType.BENCH.equals(outTacticalBoardPlayerType) && TacticalBoardPlayerType.NONE_SELECTED.equals(inTacticalBoardPlayerType)) {
            final BenchPlayer benchPlayer = benchPlayers.findPlayerById(outPlayerId);
            final NoneSelectedPlayer noneSelectedPlayer = noneSelectedPlayers.findPlayerById(inPlayerId);

            final BenchPlayer newBenchPlayerPlayer = new BenchPlayer(noneSelectedPlayer.getSquadPlayerId(), benchPlayer.getPosition());
            final NoneSelectedPlayer newNoneSelectedPlayer = new NoneSelectedPlayer(benchPlayer.getSquadPlayerId(), benchPlayer.getPosition());

            benchPlayers.remove(benchPlayer);
            noneSelectedPlayers.remove(noneSelectedPlayer);
            benchPlayers.add(newBenchPlayerPlayer);
            noneSelectedPlayers.add(newNoneSelectedPlayer);
        }
    }

    public void moveLocation(final String squadPlayerId, int x, int y) {
        final FieldPlayer filedPlayer = fieldPlayers.findPlayerById(squadPlayerId);
        if (isNull(filedPlayer)) {
            throw new IllegalArgumentException();
        }
        filedPlayer.movePosition(x, y);
        final List<FieldPlayer> currentPlayers = fieldPlayers.getFiledPlayers();
        this.formation = FormationMatcher.inferFormation(currentPlayers);
    }

    public void applyFormation(final Formation formation) {
        this.formation = formation;
        final List<FieldPlayer> currentPlayers = fieldPlayers.getFiledPlayers();
        if (currentPlayers.isEmpty()) {
            return;
        }
        final Map<String, Position> playerPositionMap = FormationMatcher.matchPlayersToFormation(currentPlayers, formation);
        for (FieldPlayer player : currentPlayers) {
            final Position newPosition = playerPositionMap.get(player.getSquadPlayerId());
            if (Objects.nonNull(newPosition)) {
                player.movePosition(newPosition.getX(), newPosition.getY());
            }
        }
    }

    public FieldPlayers getFieldPlayers() {
        return fieldPlayers;
    }

    public BenchPlayers getBenchPlayers() {
        return benchPlayers;
    }

    public NoneSelectedPlayers getNoneSelectedPlayers() {
        return noneSelectedPlayers;
    }

    public Formation getFormation() {
        return formation;
    }
}
