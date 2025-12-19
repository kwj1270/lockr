package com.official.lockr.domain.club.sport.football.lineup.domain;

import com.official.lockr.domain.club.sport.football.lineup.domain.vo.SlotType;
import com.official.lockr.global.ddd.AggregateRoot;
import com.official.lockr.global.vo.Formation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

public class Lineup extends AggregateRoot {

    private final String id;
    private final String name;
    private final String clubId;
    private Formation formation;
    private LineupSlots lineupSlots;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Lineup(final String clubId, final String name) {
        this(generateUlid(), clubId, name, Formation.FORMATION_4_3_3, new LineupSlots(), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public Lineup(final String id, final String clubId, final String name,
                  final Formation formation, final LineupSlots lineupSlots,
                  final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.clubId = clubId;
        this.name = name;
        this.formation = formation;
        this.lineupSlots = lineupSlots;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    /**
     * 특정 슬롯에 선수를 배치합니다.
     * 교체 로직:
     * - Case A: 빈 슬롯 + 신규 선수 → 추가
     * - Case B: 기존 슬롯 + 신규 선수 → 교체
     * - Case C: 빈 슬롯 + 기존 선수 → 이동
     * - Case D: 기존 슬롯 + 기존 선수 → 양방향 교체
     */
    public void assignSlot(final String squadPlayerId, final String slotTypeValue, final int slotIndex) {
        final SlotType slotType = SlotType.fromValue(slotTypeValue);
        final List<LineupSlot> currentPlayers = new ArrayList<>(this.lineupSlots.getLineupPlayers());
        final Optional<LineupSlot> targetSlotPlayer = currentPlayers.stream()
                .filter(lp -> lp.isSameSlot(slotType, slotIndex))
                .findFirst();
        final Optional<LineupSlot> existingPlayer = currentPlayers.stream()
                .filter(lp -> lp.isSameSquadPlayer(squadPlayerId))
                .findFirst();

        if (existingPlayer.isPresent() && targetSlotPlayer.isPresent() && existingPlayer.get().equals(targetSlotPlayer.get())) {
            return;
        }

        if (existingPlayer.isPresent() && targetSlotPlayer.isPresent()) {
            swapPlayers(currentPlayers, existingPlayer.get(), targetSlotPlayer.get(), squadPlayerId, slotType, slotIndex);
        } else if (existingPlayer.isPresent()) {
            movePlayer(currentPlayers, existingPlayer.get(), squadPlayerId, slotType, slotIndex);
        } else if (targetSlotPlayer.isPresent()) {
            replacePlayer(currentPlayers, targetSlotPlayer.get(), squadPlayerId, slotType, slotIndex);
        } else {
            addPlayer(currentPlayers, squadPlayerId, slotType, slotIndex);
        }

        this.lineupSlots = new LineupSlots(currentPlayers);
    }

    private void swapPlayers(final List<LineupSlot> players, final LineupSlot playerToMove, final LineupSlot playerInTarget, final String squadPlayerId, final SlotType slotType, final int slotIndex) {
        players.remove(playerToMove);
        players.remove(playerInTarget);
        players.add(LineupSlot.of(this.id, squadPlayerId, slotType, slotIndex));
        players.add(LineupSlot.of(this.id, playerInTarget.getSquadPlayerId(), playerToMove.getSlotType(), playerToMove.getSlotIndex()));
    }

    private void movePlayer(final List<LineupSlot> players, final LineupSlot existingPlayer, final String squadPlayerId, final SlotType slotType, final int slotIndex) {
        players.remove(existingPlayer);
        players.add(LineupSlot.of(this.id, squadPlayerId, slotType, slotIndex));
    }

    private void replacePlayer(final List<LineupSlot> players, final LineupSlot targetSlotPlayer, final String squadPlayerId, final SlotType slotType, final int slotIndex) {
        players.remove(targetSlotPlayer);
        players.add(LineupSlot.of(this.id, squadPlayerId, slotType, slotIndex));
    }

    private void addPlayer(final List<LineupSlot> players, final String squadPlayerId, final SlotType slotType, final int slotIndex) {
        players.add(LineupSlot.of(this.id, squadPlayerId, slotType, slotIndex));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getClubId() {
        return clubId;
    }

    public Formation getFormation() {
        return formation;
    }

    public List<LineupSlot> getLineupPlayers() {
        return lineupSlots.getLineupPlayers();
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public java.time.LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
