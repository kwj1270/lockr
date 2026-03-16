package com.official.lockr.domain.club.sport.football.squad.domain;

import com.official.lockr.global.ddd.AggregateRoot;
import com.official.lockr.global.vo.BackNumber;
import com.official.lockr.global.vo.BirthDate;
import com.official.lockr.global.vo.Foot;
import com.official.lockr.global.vo.Position;
import reactor.util.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;

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

    public boolean hasPlayer(final String userId) {
        return squadPlayers.stream().anyMatch(it -> it.isSame(userId));
    }

    public void addPlayer(final SquadPlayer lineUpMember) {
        squadPlayers.add(lineUpMember);
    }

    public String getId() {
        return id;
    }

    public String getClubId() {
        return clubId;
    }

    public List<SquadPlayer> getSquadPlayers() {
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

    @Nullable
    public SquadPlayer findByUserId(final String userId) {
        return squadPlayers.stream()
                .filter(it -> it.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public void removePlayer(final String userId) {
        squadPlayers.removeIf(it -> it.isSame(userId));
    }

    public void updatePlayer(final String userId, final String birthDate, final String height, final String weight, final Foot foot, final List<Position> positions, final Integer backNumber) {
        final SquadPlayer squadPlayer = findByUserId(userId);
        if(isNull(squadPlayer)) {
            throw new IllegalArgumentException();
        }
        squadPlayer.update(birthDate, height, weight, foot, positions, new BackNumber(backNumber));
    }
}
