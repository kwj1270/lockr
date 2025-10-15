package com.official.lockr.domain.club.squard.domain.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StartingPlayers {

    private static final int STARTING_PLAYER_COUNT = 11;

    private final List<StartingPlayer> startingPlayers;

    public StartingPlayers(final List<StartingPlayer> startingPlayers) {
        verify(startingPlayers);
        this.startingPlayers = startingPlayers;
    }

    private void verify(final List<StartingPlayer> startingPlayers) {
        final long startingCount = startingPlayers.size();
        if (startingCount != STARTING_PLAYER_COUNT) {
            throw new IllegalArgumentException("Entry must have exactly " + STARTING_PLAYER_COUNT + " starting players");
        }
        final long captainCount = startingPlayers.stream()
                .filter(StartingPlayer::isCaptain)
                .count();
        if (captainCount != 1) {
            assignCaptainToStartingGK();
        }
    }

    public static StartingPlayers of(final List<EntryPlayer> entryPlayers) {
        return new StartingPlayers(entryPlayers.stream()
                .filter(player -> player instanceof StartingPlayer)
                .map(player -> (StartingPlayer) player)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    public StartingPlayer findPlayerById(final String playerId) {
        return startingPlayers.stream()
                .filter(player -> player.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found in entry: " + playerId));
    }

    public void assignCaptainToStartingGK() {
        startingPlayers.stream()
                .filter(StartingPlayer::isGk)
                .findFirst()
                .ifPresent(StartingPlayer::assignCaptain);
    }

    public void remove(final StartingPlayer startingPlayer) {
        startingPlayers.remove(startingPlayer);
    }

    public void add(final StartingPlayer newStartingPlayer) {
        startingPlayers.add(newStartingPlayer);
    }
}
