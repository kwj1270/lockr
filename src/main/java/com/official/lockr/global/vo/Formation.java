package com.official.lockr.global.vo;

import java.util.Arrays;
import java.util.List;

public enum Formation {
    FORMATION_4_3_3("433", Arrays.asList(
            Position.GK,
            Position.LB, Position.LCB, Position.RCB, Position.RB,
            Position.LCM, Position.CDM, Position.RCM,
            Position.LW, Position.ST, Position.RW
    )),
    FORMATION_4_4_2("442", Arrays.asList(
            Position.GK,
            Position.LB, Position.LCB, Position.RCB, Position.RB,
            Position.LM, Position.LCM, Position.RCM, Position.RM,
            Position.LF, Position.RF
    )),
    FORMATION_4_2_3_1("4231", Arrays.asList(
            Position.GK,
            Position.LB, Position.LCB, Position.RCB, Position.RB,
            Position.LDM, Position.RDM,
            Position.LAM, Position.CAM, Position.RAM,
            Position.ST
    )),
    FORMATION_3_5_2("352", Arrays.asList(
            Position.GK,
            Position.LCB, Position.CB, Position.RCB,
            Position.LWB, Position.LM, Position.CM, Position.RM, Position.RWB,
            Position.LF, Position.RF
    )),
    FORMATION_3_4_3("343", Arrays.asList(
            Position.GK,
            Position.LCB, Position.CB, Position.RCB,
            Position.LM, Position.LCM, Position.RCM, Position.RM,
            Position.LW, Position.ST, Position.RW
    )),
    FORMATION_5_3_2("532", Arrays.asList(
            Position.GK,
            Position.LWB, Position.LCB, Position.CB, Position.RCB, Position.RWB,
            Position.LM, Position.CM, Position.RM,
            Position.LF, Position.RF
    ));

    private final String name;
    private final List<Position> positions;

    public static Formation of(final String name) {
        return Arrays.stream(values())
                .filter(it -> it.name.equals(name))
                .findFirst()
                .orElse(FORMATION_4_3_3);
    }

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
