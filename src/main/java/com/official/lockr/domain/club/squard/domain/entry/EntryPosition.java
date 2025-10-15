package com.official.lockr.domain.club.squard.domain.entry;

import com.official.lockr.domain.club.common.Position;

public enum EntryPosition {
    // 공격수 (FW) - 상단
    ST(Position.ST, 50, 10),
    LW(Position.LW, 20, 10),
    RW(Position.RW, 80, 10),
    CF(Position.CF, 50, 10),
    LF(Position.LF, 30, 10),
    RF(Position.RF, 70, 10),

    // 공격형 미드필더 (AM)
    CAM(Position.CAM, 50, 25),
    LAM(Position.LAM, 30, 25),
    RAM(Position.RAM, 70, 25),

    // 중앙 미드필더 (MF)
    CM(Position.CM, 50, 40),
    LM(Position.LM, 20, 40),
    RM(Position.RM, 80, 40),

    // 수비형 미드필더 (DM)
    CDM(Position.CDM, 50, 55),
    LDM(Position.LDM, 35, 55),
    RDM(Position.RDM, 65, 55),

    // 윙백 (WB)
    LWB(Position.LWB, 15, 60),
    RWB(Position.RWB, 85, 60),

    // 수비수 (DF)
    LB(Position.LB, 15, 75),
    RB(Position.RB, 85, 75),
    CB(Position.CB, 50, 80),
    LCB(Position.LCB, 35, 80),
    RCB(Position.RCB, 65, 80),
    SW(Position.SW, 50, 85),

    // 골키퍼 (GK) - 하단
    GK(Position.GK, 50, 95);

    private final Position position;
    private final int x; // 가로 좌표 (0-100, 왼쪽부터)
    private final int y; // 세로 좌표 (0-100, 공격 방향 기준)

    EntryPosition(final Position position, final int x, final int y) {
        this.position = position;
        this.x = x;
        this.y = y;
    }

    public Position getPosition() {
        return position;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isGK() {
        return this == GK;
    }
}
