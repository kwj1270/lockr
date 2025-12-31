package com.official.lockr.global.vo;


import static com.official.lockr.global.vo.Position.*;

public enum FootBallPosition {
    // 공격수 (FW) - 상단
    LW(FW), LF(FW), ST(FW), CF(FW), RF(FW), RW(FW),
    LM(MF), LAM(MF), CAM(MF), RAM(MF), RM(MF),
    LCM(MF), CM(MF), RCM(MF),
    LDM(MF), CDM(MF), RDM(MF),
    LWB(DF), LB(DF), CB(DF), LCB(DF), RCB(DF), RWB(DF), RB(DF),
    GK(Position.GK);

    private final Position position;

    FootBallPosition(final Position position) {
        this.position = position;
    }
}

