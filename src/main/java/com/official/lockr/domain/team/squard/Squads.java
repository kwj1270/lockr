package com.official.lockr.domain.team.squard;

import java.util.List;

public class Squads {

    private final String teamId;
    private final List<Squad> squads;

    public Squads(final String teamId, final List<Squad> squads) {
        this.teamId = teamId;
        this.squads = squads;
    }
}
