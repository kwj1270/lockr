package com.official.lockr.domain.team.player;

import com.official.lockr.domain.team.common.Position;

public class Player {

    private final String memberId;
    private final Position position;
    private final Stat stat;
    private final BackNumber backNumber;

    public Player(final String memberId, final Position position, final Stat stat, final BackNumber backNumber) {
        this.memberId = memberId;
        this.position = position;
        this.stat = stat;
        this.backNumber = backNumber;
    }
}
