package com.official.lockr.domain.club.squard.domain.entry;

import java.util.List;

public class EntryPlayers {

    private static final int MAX_ENTRY_SIZE = 18;

    private final StartingPlayers startingPlayers;
    private final SubstitutePlayers substitutePlayers;

    public EntryPlayers(final List<EntryPlayer> entryPlayers) {
        verify(entryPlayers);
        this.startingPlayers = StartingPlayers.of(entryPlayers);
        this.substitutePlayers = SubstitutePlayers.of(entryPlayers);
    }

    private void verify(final List<EntryPlayer> players) {
        if (players.size() > MAX_ENTRY_SIZE) {
            throw new IllegalArgumentException("Entry cannot have more than " + MAX_ENTRY_SIZE + " players");
        }
    }

    public void substitute(final String outPlayerId, final String inPlayerId) {
        final StartingPlayer startingPlayer = startingPlayers.findPlayerById(outPlayerId);
        final SubstitutePlayer substitutePlayer = substitutePlayers.findPlayerById(inPlayerId);

        if (startingPlayer.isCaptain()) {
            startingPlayer.releaseCaptain();
            startingPlayers.assignCaptainToStartingGK();
        }

        final StartingPlayer newStartingPlayer = new StartingPlayer(
                substitutePlayer.getEntryId(),
                substitutePlayer.getPlayerId(),
                startingPlayer.getEntryPosition(),
                startingPlayer.getLocation(),
                false
        );

        final SubstitutePlayer newSubstitutePlayer = new SubstitutePlayer(
                startingPlayer.getEntryId(),
                startingPlayer.getPlayerId(),
                startingPlayer.getEntryPosition().getPosition()
        );

        startingPlayers.remove(startingPlayer);
        substitutePlayers.remove(substitutePlayer);
        startingPlayers.add(newStartingPlayer);
        substitutePlayers.add(newSubstitutePlayer);
    }
}
