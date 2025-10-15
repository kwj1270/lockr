package com.official.lockr.domain.club.squard.domain.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubstitutePlayers {

    private static final int MAX_SUBSTITUTE_COUNT = 7;
    private final List<SubstitutePlayer> substitutePlayers;

    public SubstitutePlayers(final List<SubstitutePlayer> substitutePlayers) {
        verify(substitutePlayers);
        this.substitutePlayers = substitutePlayers;
    }

    private static void verify(final List<SubstitutePlayer> substitutePlayers) {
        final long substituteCount = substitutePlayers.size();
        if (substituteCount > MAX_SUBSTITUTE_COUNT) {
            throw new IllegalArgumentException("Entry cannot have more than " + MAX_SUBSTITUTE_COUNT + " substitute players");
        }
    }

    public static SubstitutePlayers of(final List<EntryPlayer> entryPlayers) {
        return new SubstitutePlayers(entryPlayers.stream()
                .filter(player -> player instanceof SubstitutePlayer)
                .map(player -> (SubstitutePlayer) player)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    public SubstitutePlayer findPlayerById(final String playerId) {
        return substitutePlayers.stream()
                .filter(player -> player.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found in entry: " + playerId));
    }

    public void add(final SubstitutePlayer newSubstitutePlayer) {
        substitutePlayers.add(newSubstitutePlayer);
    }

    public void remove(final SubstitutePlayer substitutePlayer) {
        substitutePlayers.remove(substitutePlayer);
    }
}
