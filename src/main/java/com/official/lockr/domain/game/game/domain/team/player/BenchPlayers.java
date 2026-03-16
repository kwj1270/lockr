package com.official.lockr.domain.game.game.domain.team.player;

import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BenchPlayers {

    private static final int MAX_SUBSTITUTE_COUNT = 7;
    private final List<BenchPlayer> benchPlayers;

    public BenchPlayers(final List<BenchPlayer> benchPlayers) {
        this.benchPlayers = benchPlayers;
    }

    public static BenchPlayers of(final List<GamePlayer> entryGamePlayers) {
        return new BenchPlayers(entryGamePlayers.stream()
                .filter(GamePlayer::isSubstitute)
                .map(player -> (BenchPlayer) player)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    @Nullable
    public BenchPlayer findPlayerById(final String playerId) {
        return benchPlayers.stream()
                .filter(player -> player.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public void add(final BenchPlayer newBenchPlayer) {
        if (benchPlayers.size() >= MAX_SUBSTITUTE_COUNT) {
            throw new IllegalStateException("Cannot add more than " + MAX_SUBSTITUTE_COUNT + " substitute players");
        }

        final boolean isDuplicate = benchPlayers.stream()
                .anyMatch(player -> player.getPlayerId().equals(newBenchPlayer.getPlayerId()));
        if (isDuplicate) {
            throw new IllegalArgumentException("Player already exists in substitute players");
        }

        benchPlayers.add(newBenchPlayer);
    }

    public void remove(final BenchPlayer benchPlayer) {
        if (!benchPlayers.contains(benchPlayer)) {
            throw new IllegalArgumentException("Player not found in substitute players");
        }
        benchPlayers.remove(benchPlayer);
    }

    public boolean isRegistered() {
        return !(benchPlayers.isEmpty());
    }
}
