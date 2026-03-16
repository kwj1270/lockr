package com.official.lockr.domain.club.sqaud.domain.board.player;

import java.util.ArrayList;
import java.util.List;

public class BenchPlayers {

    private final List<BenchPlayer> benchPlayers;

    public BenchPlayers() {
        this(new ArrayList<>());
    }

    public BenchPlayers(final List<BenchPlayer> benchPlayers) {
        this.benchPlayers = benchPlayers;
    }

    public BenchPlayer findPlayerById(final String playerId) {
        return benchPlayers.stream()
                .filter(player -> player.getSquadPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found in entry: " + playerId));
    }

    public void add(final BenchPlayer newBenchPlayer) {
        benchPlayers.add(newBenchPlayer);
    }

    public void remove(final BenchPlayer benchPlayer) {
        benchPlayers.remove(benchPlayer);
    }

    public List<BenchPlayer> getBenchPlayers() {
        return new ArrayList<>(benchPlayers);
    }

    public void addMatchPlayer(final String squadPlayerId) {

    }
}
