package com.official.lockr.domain.team.player;

public class Stat {

    private final String birth;
    private final String height;
    private final String weight;
    private final String leftFoot;
    private final String rightFoot;

    public Stat(final String birth, final String height, final String weight, final String leftFoot, final String rightFoot) {
        this.birth = birth;
        this.height = height;
        this.weight = weight;
        this.leftFoot = leftFoot;
        this.rightFoot = rightFoot;
    }
}
