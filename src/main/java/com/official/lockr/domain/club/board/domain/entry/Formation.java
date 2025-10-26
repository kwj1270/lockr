package com.official.lockr.domain.club.board.domain.entry;

import com.official.lockr.domain.club.common.Position;

import java.util.Arrays;
import java.util.List;

public enum Formation {
    FORMATION_4_3_3("4-3-3", Arrays.asList(
            Position.GK,
            Position.LB, Position.LCB, Position.RCB, Position.RB,
            Position.LCM, Position.CDM, Position.RCM,
            Position.LW, Position.ST, Position.RW
    )),
    FORMATION_4_4_2("4-4-2", Arrays.asList(
            Position.GK,
            Position.LB, Position.LCB, Position.RCB, Position.RB,
            Position.LM, Position.LCM, Position.RCM, Position.RM,
            Position.LF, Position.RF
    )),
    FORMATION_4_2_3_1("4-2-3-1", Arrays.asList(
            Position.GK,
            Position.LB, Position.LCB, Position.RCB, Position.RB,
            Position.LDM, Position.RDM,
            Position.LAM, Position.CAM, Position.RAM,
            Position.ST
    )),
    FORMATION_3_5_2("3-5-2", Arrays.asList(
            Position.GK,
            Position.LCB, Position.CB, Position.RCB,
            Position.LWB, Position.LM, Position.CM, Position.RM, Position.RWB,
            Position.LF, Position.RF
    )),
    FORMATION_3_4_3("3-4-3", Arrays.asList(
            Position.GK,
            Position.LCB, Position.CB, Position.RCB,
            Position.LM, Position.LCM, Position.RCM, Position.RM,
            Position.LW, Position.ST, Position.RW
    )),
    FORMATION_5_3_2("5-3-2", Arrays.asList(
            Position.GK,
            Position.LWB, Position.LCB, Position.CB, Position.RCB, Position.RWB,
            Position.LM, Position.CM, Position.RM,
            Position.LF, Position.RF
    ));

    private final String name;
    private final List<Position> positions;

    Formation(final String name, final List<Position> positions) {
        this.name = name;
        this.positions = positions;
    }

    public String getName() {
        return name;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public int getRequiredPlayerCount() {
        return positions.size();
    }

    public static Formation fromName(final String name) {
        return Arrays.stream(Formation.values())
                .filter(formation -> formation.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid formation name: " + name));
    }
}
