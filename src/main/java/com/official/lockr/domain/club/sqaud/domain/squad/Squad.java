package com.official.lockr.domain.club.sqaud.domain.squad;

import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.List;

public class Squad extends AggregateRoot {

    private final String id;
    private final String clubId;
    private final List<SquadPlayer> squadPlayers;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Squad(final String id, final String clubId, final List<SquadPlayer> squadPlayers,
                 final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.clubId = clubId;
        this.squadPlayers = squadPlayers;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public boolean hasPlayer(final String memberId) {
        return squadPlayers.stream().anyMatch(it -> it.isSameMember(memberId));
    }

    public void addPlayer(final SquadPlayer squadPlayer) {
        squadPlayers.add(squadPlayer);
    }

    public String getId() {
        return id;
    }

    public String getClubId() {
        return clubId;
    }

    public List<SquadPlayer> getPlayers() {
        return squadPlayers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
