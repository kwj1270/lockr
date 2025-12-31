package com.official.lockr.global.vo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 축구 포메이션을 정의하는 Enum
 * 각 포메이션은 이름과 요구되는 포지션 목록을 가집니다.
 */
public enum Formation {
    FORMATION_4_3_3("433", Arrays.asList(
            FootBallPosition.GK,
            FootBallPosition.LB, FootBallPosition.LCB, FootBallPosition.RCB, FootBallPosition.RB,
            FootBallPosition.LCM, FootBallPosition.CDM, FootBallPosition.RCM,
            FootBallPosition.LW, FootBallPosition.ST, FootBallPosition.RW
    )),
    FORMATION_4_4_2("442", Arrays.asList(
            FootBallPosition.GK,
            FootBallPosition.LB, FootBallPosition.LCB, FootBallPosition.RCB, FootBallPosition.RB,
            FootBallPosition.LM, FootBallPosition.LCM, FootBallPosition.RCM, FootBallPosition.RM,
            FootBallPosition.LF, FootBallPosition.RF
    )),
    FORMATION_4_2_3_1("4231", Arrays.asList(
            FootBallPosition.GK,
            FootBallPosition.LB, FootBallPosition.LCB, FootBallPosition.RCB, FootBallPosition.RB,
            FootBallPosition.LDM, FootBallPosition.RDM,
            FootBallPosition.LAM, FootBallPosition.CAM, FootBallPosition.RAM,
            FootBallPosition.ST
    )),
    FORMATION_3_5_2("352", Arrays.asList(
            FootBallPosition.GK,
            FootBallPosition.LCB, FootBallPosition.CB, FootBallPosition.RCB,
            FootBallPosition.LWB, FootBallPosition.LM, FootBallPosition.CM, FootBallPosition.RM, FootBallPosition.RWB,
            FootBallPosition.LF, FootBallPosition.RF
    )),
    FORMATION_3_4_3("343", Arrays.asList(
            FootBallPosition.GK,
            FootBallPosition.LCB, FootBallPosition.CB, FootBallPosition.RCB,
            FootBallPosition.LM, FootBallPosition.LCM, FootBallPosition.RCM, FootBallPosition.RM,
            FootBallPosition.LW, FootBallPosition.ST, FootBallPosition.RW
    )),
    FORMATION_5_3_2("532", Arrays.asList(
            FootBallPosition.GK,
            FootBallPosition.LWB, FootBallPosition.LCB, FootBallPosition.CB, FootBallPosition.RCB, FootBallPosition.RWB,
            FootBallPosition.LM, FootBallPosition.CM, FootBallPosition.RM,
            FootBallPosition.LF, FootBallPosition.RF
    ));

    private final String name;
    private final List<FootBallPosition> positions;

    Formation(final String name, final List<FootBallPosition> positions) {
        this.name = name;
        this.positions = Collections.unmodifiableList(positions);
    }

    /**
     * 이름으로 포메이션을 찾습니다. 찾지 못하면 기본 포메이션(4-3-3)을 반환합니다.
     */
    public static Formation of(final String name) {
        return Arrays.stream(values())
                .filter(formation -> formation.name.equals(name))
                .findFirst()
                .orElse(FORMATION_4_3_3);
    }

    /**
     * 이름으로 포메이션을 찾습니다. 찾지 못하면 예외를 던집니다.
     */
    public static Formation fromName(final String name) {
        return Arrays.stream(values())
                .filter(formation -> formation.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid formation name: " + name));
    }

    public String getName() {
        return name;
    }

    public List<FootBallPosition> getPositions() {
        return positions;
    }

    public int getRequiredPlayerCount() {
        return positions.size();
    }
}
