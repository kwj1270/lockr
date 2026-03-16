package com.official.lockr.domain.club.sqaud.domain.board.player;

import com.official.lockr.domain.club.common.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FieldPlayers {

    private final List<FieldPlayer> fieldPlayers;

    public FieldPlayers() {
        this(new ArrayList<>());
    }

    public FieldPlayers(final List<FieldPlayer> fieldPlayers) {
        this.fieldPlayers = fieldPlayers;
    }

    public FieldPlayer findPlayerById(final String playerId) {
        return fieldPlayers.stream()
                .filter(player -> player.getSquadPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found in entry: " + playerId));
    }

    public void assignCaptainToStartingGK() {
        fieldPlayers.stream()
                .filter(FieldPlayer::isGk)
                .findFirst()
                .ifPresent(FieldPlayer::assignCaptain);
    }

    public void remove(final FieldPlayer fieldPlayer) {
        fieldPlayers.remove(fieldPlayer);
    }

    public void add(final FieldPlayer newFieldPlayer) {
        fieldPlayers.add(newFieldPlayer);
    }

    public List<FieldPlayer> getFiledPlayers() {
        return new ArrayList<>(fieldPlayers);
    }

    public void addMatchPlayer(final String squadPlayerId, final Integer x, final Integer y) {
        final Optional<FieldPlayer> existPlayer = fieldPlayers.stream()
                .filter(it -> it.isSame(squadPlayerId))
                .findFirst();
        if (existPlayer.isPresent()) {
            final FieldPlayer fieldPlayer = existPlayer.get();
            fieldPlayer.movePosition(x, y);
            return;
        }
        final Position position = Position.calculate(x, y);
        final Location location = new Location(x, y);
        final boolean conflictLocation = fieldPlayers.stream().anyMatch(it -> it.conflictLocation(location));
        if(conflictLocation) {
            throw new IllegalStateException();
        }
        final FieldPlayer fieldPlayer = new FieldPlayer(squadPlayerId, position, location, false);
        fieldPlayers.add(fieldPlayer);
    }
}
