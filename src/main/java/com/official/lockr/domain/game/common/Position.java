package com.official.lockr.domain.game.common;

public enum Position {
    // 공격수 (FW) - 상단
    ST(50, 10),
    LW(20, 10),
    RW(80, 10),
    CF(50, 10),
    LF(30, 10),
    RF(70, 10),

    // 공격형 미드필더 (AM)
    CAM(50, 25),
    LAM(30, 25),
    RAM(70, 25),

    // 중앙 미드필더 (MF)
    CM(50, 40),
    LM(20, 40),
    RM(80, 40),

    // 수비형 미드필더 (DM)
    CDM(50, 55),
    LDM(35, 55),
    RDM(65, 55),

    // 윙백 (WB)
    LWB(15, 60),
    RWB(85, 60),

    // 수비수 (DF)
    LB(15, 75),
    RB(85, 75),
    CB(50, 80),
    LCB(35, 80),
    RCB(65, 80),
    SW(50, 85),

    // 골키퍼 (GK) - 하단
    GK(50, 95);

    private final int x; // 가로 좌표 (0-100, 왼쪽부터)
    private final int y; // 세로 좌표 (0-100, 공격 방향 기준)

    Position(final int x, final int y) {
        this.x = x;
        this.y = y;
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
