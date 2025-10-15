package com.official.lockr.domain.club.squard.domain.entry;

import com.official.lockr.global.ddd.AggregateRoot;

import java.util.List;

public class Entry extends AggregateRoot {

    private final String id;
    private final String squadId;
    private final List<EntryPlayer> entryPlayers;

    public Entry(final String id, final String squadId, final List<EntryPlayer> entryPlayers) {
        this.id = id;
        this.squadId = squadId;
        this.entryPlayers = entryPlayers;
    }

    public String getId() {
        return id;
    }

    public String getSquadId() {
        return squadId;
    }

    public List<EntryPlayer> getEntryPlayers() {
        return entryPlayers;
    }
}
