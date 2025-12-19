package com.official.lockr.domain.club.sport.football.lineup.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 라인업 내 선수 배치 정보를 나타내는 엔티티
 * 선발(starter) 또는 후보(substitute) 슬롯에 선수를 배치합니다.
 */
public class LineupSlots {

    private final List<LineupSlot> lineupSlots;

    public LineupSlots() {
        this(new ArrayList<>());
    }

    public LineupSlots(final List<LineupSlot> lineupSlots) {
        this.lineupSlots = lineupSlots;
    }

    public List<LineupSlot> getLineupPlayers() {
        return lineupSlots;
    }

    public boolean isEmpty() {
        return lineupSlots.isEmpty();
    }

    public java.util.stream.Stream<LineupSlot> stream() {
        return lineupSlots.stream();
    }
}
