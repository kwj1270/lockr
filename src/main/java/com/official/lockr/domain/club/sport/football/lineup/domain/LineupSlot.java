package com.official.lockr.domain.club.sport.football.lineup.domain;

import com.official.lockr.domain.club.sport.football.lineup.domain.vo.SlotType;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.time.LocalDateTime.now;

/**
 * 라인업 내 선수 배치 정보를 나타내는 엔티티
 * 선발(starter) 또는 후보(substitute) 슬롯에 선수를 배치합니다.
 */
public class LineupSlot {

    private final String id;
    private final String lineupId;
    private final String squadPlayerId;
    private final SlotType slotType;
    private final int slotIndex;
    private final LocalDateTime createdAt;

    public static LineupSlot of(final String lineupId, final String squadPlayerId, final SlotType slotType, final int slotIndex) {
        return new LineupSlot(generateUlid(), lineupId, squadPlayerId, slotType, slotIndex, now());
    }

    public LineupSlot(final String id, final String lineupId, final String squadPlayerId, final SlotType slotType, final int slotIndex, final LocalDateTime createdAt) {
        validateSlotIndex(slotType, slotIndex);
        this.id = id;
        this.lineupId = lineupId;
        this.squadPlayerId = squadPlayerId;
        this.slotType = slotType;
        this.slotIndex = slotIndex;
        this.createdAt = createdAt;
    }

    /**
     * 슬롯 타입에 따라 슬롯 인덱스의 유효성을 검증합니다.
     * - STARTER: 0~10 (선발 11명)
     * - SUBSTITUTE: 0~6 (후보 7명)
     */
    private void validateSlotIndex(final SlotType slotType, final int slotIndex) {
        switch (slotType) {
            case STARTER -> {
                if (slotIndex < 0 || slotIndex > 10) {
                    throw new IllegalArgumentException("선발 슬롯 인덱스는 0~10 사이여야 합니다. 입력값: " + slotIndex);
                }
            }
            case SUBSTITUTE -> {
                if (slotIndex < 0 || slotIndex > 6) {
                    throw new IllegalArgumentException("후보 슬롯 인덱스는 0~6 사이여야 합니다. 입력값: " + slotIndex);
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getLineupId() {
        return lineupId;
    }

    public String getSquadPlayerId() {
        return squadPlayerId;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isSameSquadPlayer(final String squadPlayerId) {
        return this.squadPlayerId.equals(squadPlayerId);
    }

    public boolean isSameSlot(final SlotType slotType, final int slotIndex) {
        return this.slotType == slotType && this.slotIndex == slotIndex;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final LineupSlot that = (LineupSlot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
