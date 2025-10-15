package com.official.lockr.domain.club.squard.domain.squad;

import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.List;

public class Squad extends AggregateRoot {

    private final String id;
    private final String teamId;
    private final List<Player> players;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Squad(final String id, final String teamId, final List<Player> players,
                 final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.teamId = teamId;
        this.players = players;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public boolean hasPlayer(final String memberId) {
        return players.stream().anyMatch(it -> it.isSameMember(memberId));
    }

    public void addPlayer(final Player player) {
        players.add(player);
    }

    public String getId() {
        return id;
    }

    public String getTeamId() {
        return teamId;
    }

    public List<Player> getPlayers() {
        return players;
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
