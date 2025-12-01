package com.official.lockr.domain.club.lineup.tacticalboard.domain;

import com.official.lockr.domain.club.lineup.tacticalboard.domain.entry.Entry;
import com.official.lockr.global.vo.Formation;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;

public class TacticalBoard extends AggregateRoot {

    private final String id;
    private final String clubId;
    private String coachUserId;
    private final String name;
    private final Entry entry;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public static TacticalBoard init(final String id, final String clubId, final String coachUserId, final String name) {
        return new TacticalBoard(id, clubId, coachUserId, name, new Entry(), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public TacticalBoard(final String id, final String clubId, final String coachUserId, final String name,
                         final Entry entry, final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.clubId = clubId;
        this.coachUserId = coachUserId;
        this.name = name;
        this.entry = entry;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public void addPlayer(final String coachUserId, final String squadPlayerId, final String tacticalBoardPlayerType, final int x, final int y) {
        this.coachUserId = coachUserId;
        this.updatedAt = LocalDateTime.now();
        entry.addPlayer(squadPlayerId, tacticalBoardPlayerType, x, y);
    }

    public void substitute(final String coachUserId, final String outPlayerId, final String outPlayerType, final String inPlayerId, final String inPlayerType) {
        this.coachUserId = coachUserId;
        this.updatedAt = LocalDateTime.now();
        entry.substitute(outPlayerId, outPlayerType, inPlayerId, inPlayerType);
    }

    public void moveLocation(final String coachUserId, final String squadPlayerId, int x, int y) {
        this.coachUserId = coachUserId;
        this.updatedAt = LocalDateTime.now();
        entry.moveLocation(squadPlayerId, x, y);
    }

    public void applyFormation(final String coachUserId, final Formation formation) {
        this.coachUserId = coachUserId;
        this.updatedAt = LocalDateTime.now();
        entry.applyFormation(formation);
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

    public Entry getLineup() {
        return entry;
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
