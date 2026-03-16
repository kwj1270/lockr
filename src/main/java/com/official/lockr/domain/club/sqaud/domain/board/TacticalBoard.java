package com.official.lockr.domain.club.sqaud.domain.board;

import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;

public class TacticalBoard extends AggregateRoot {

    private final String id;
    private final String squadId;
    private String coachUserId;
    private final String name;
    private final Lineup lineup;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public static TacticalBoard init(final String id, final String squadId, final String coachUserId, final String name) {
        return new TacticalBoard(id, squadId, coachUserId, name, new Lineup(), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public TacticalBoard(final String id, final String squadId, final String coachUserId, final String name,
                         final Lineup lineup, final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.squadId = squadId;
        this.coachUserId = coachUserId;
        this.name = name;
        this.lineup = lineup;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public void addMatchPlayer(final String coachUserId, final String squadPlayerId, final String tacticalBoardPlayerType, final int x, final int y) {
        this.coachUserId = coachUserId;
        this.updatedAt = LocalDateTime.now();
        lineup.addMatchPlayer(squadPlayerId, tacticalBoardPlayerType, x, y);
    }

    public void substitute(final String coachUserId, final String outPlayerId, final String outPlayerType, final String inPlayerId, final String inPlayerType) {
        this.coachUserId = coachUserId;
        this.updatedAt = LocalDateTime.now();
        lineup.substitute(outPlayerId, outPlayerType, inPlayerId, inPlayerType);
    }

    public void moveLocation(final String coachUserId, final String squadPlayerId, int x, int y) {
        this.coachUserId = coachUserId;
        this.updatedAt = LocalDateTime.now();
        lineup.moveLocation(squadPlayerId, x, y);
    }

    public void applyFormation(final String coachUserId, final Formation formation) {
        this.coachUserId = coachUserId;
        this.updatedAt = LocalDateTime.now();
        lineup.applyFormation(formation);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSquadId() {
        return squadId;
    }

    public Lineup getLineup() {
        return lineup;
    }

    public String getCoachUserId() {
        return coachUserId;
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
