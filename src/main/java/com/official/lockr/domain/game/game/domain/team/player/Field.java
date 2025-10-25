package com.official.lockr.domain.game.game.domain.team.player;

import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Field {

    private static final int STARTING_PLAYER_COUNT = 11;

    private final List<FieldPlayer> fieldPlayers;

    public Field(final List<FieldPlayer> fieldPlayers) {
        this.fieldPlayers = fieldPlayers;
    }

    public static Field of(final List<Player> entryPlayers) {
        return new Field(entryPlayers.stream()
                .filter(Player::isStarting)
                .map(player -> (FieldPlayer) player)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    @Nullable
    public FieldPlayer findPlayerById(final String playerId) {
        return fieldPlayers.stream()
                .filter(player -> player.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public void assignCaptainToStartingGK() {
        fieldPlayers.stream()
                .filter(FieldPlayer::isGk)
                .findFirst()
                .ifPresent(FieldPlayer::assignCaptain);
    }

    public void remove(final FieldPlayer fieldPlayer) {
        if (!fieldPlayers.contains(fieldPlayer)) {
            throw new IllegalArgumentException("Player not found in field players");
        }
        fieldPlayers.remove(fieldPlayer);
    }

    public void add(final FieldPlayer newFieldPlayer) {
        if (fieldPlayers.size() >= STARTING_PLAYER_COUNT) {
            throw new IllegalStateException("Cannot add more than " + STARTING_PLAYER_COUNT + " field players");
        }

        final boolean isDuplicate = fieldPlayers.stream()
                .anyMatch(player -> player.getPlayerId().equals(newFieldPlayer.getPlayerId()));
        if (isDuplicate) {
            throw new IllegalArgumentException("Player already exists in field players");
        }

        fieldPlayers.add(newFieldPlayer);
    }

    public boolean isRegistered() {
        return !(fieldPlayers.isEmpty());
    }
}
