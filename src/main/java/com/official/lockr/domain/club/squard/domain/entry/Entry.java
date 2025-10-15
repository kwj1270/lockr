package com.official.lockr.domain.club.squard.domain.entry;

import com.official.lockr.global.ddd.AggregateRoot;

import java.util.List;

public class Entry extends AggregateRoot {

    private final String id;
    private final String name;
    private final String squadId;
    private final EntryPlayers entryPlayers;

    public Entry(final String id, final String name, final String squadId, final List<EntryPlayer> players) {
        this.id = id;
        this.name = name;
        this.squadId = squadId;
        this.entryPlayers = new EntryPlayers(players);
    }

    public void substitutePlayer(final String outPlayerId, final String inPlayerId) {
        entryPlayers.substitute(outPlayerId, inPlayerId);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSquadId() {
        return squadId;
    }
}
