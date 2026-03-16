package com.official.lockr.domain.club.tacticalboard.domain.entry.player;

import java.util.ArrayList;
import java.util.List;

public class NoneSelectedPlayers {

    private final List<NoneSelectedPlayer> noneSelectedPlayers;

    public NoneSelectedPlayers() {
        this(new ArrayList<>());
    }

    public NoneSelectedPlayers(final List<NoneSelectedPlayer> noneSelectedPlayers) {
        this.noneSelectedPlayers = noneSelectedPlayers;
    }

    public NoneSelectedPlayer findPlayerById(final String playerId) {
        return noneSelectedPlayers.stream()
                .filter(player -> player.getSquadPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found in entry: " + playerId));
    }

    public void remove(final NoneSelectedPlayer noneSelectedPlayer) {
        noneSelectedPlayers.remove(noneSelectedPlayer);
    }

    public void add(final NoneSelectedPlayer noneSelectedPlayer) {
        noneSelectedPlayers.add(noneSelectedPlayer);
    }

    public void addPlayer(final String squadPlayerId) {

    }

    public List<NoneSelectedPlayer> getNoneSelectedPlayers() {
        return new java.util.ArrayList<>(noneSelectedPlayers);
    }
}
