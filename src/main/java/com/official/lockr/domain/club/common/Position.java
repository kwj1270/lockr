package com.official.lockr.domain.club.common;

public enum Position {
    ST,
    LW,
    RW,
    CF,
    LF,
    RF,
    CAM,
    CM,
    LM,
    RM,
    CDM,
    LWB,
    RWB,
    LB,
    RB,
    CB,
    LCB,
    RCB,
    SW,
    GK;

    public static Object of(final String name) {
        return valueOf(name);
    }
}
