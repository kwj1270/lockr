package com.official.lockr.domain.club.match.domain;

import com.official.lockr.domain.club.match.domain.event.AcceptedMatchEvent;
import com.official.lockr.domain.club.match.domain.event.InviteMatchEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.domain.club.match.domain.MatchStatus.PENDING;

public class Match extends AggregateRoot {

    private final String id;
    private final String homeClubId;
    private final String homeClubMangerUserId;
    private final String awayClubId;
    private String awayClubMangerUserId;
    private final String location;
    private MatchStatus status;
    private final LocalDateTime matchDateTime;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Match(final String id, final String homeClubId, final String homeClubMangerUserId, final String awayClubId, final String awayClubMangerUserId,
                 final String location, final MatchStatus status, final LocalDateTime matchDateTime,
                 final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.homeClubId = homeClubId;
        this.homeClubMangerUserId = homeClubMangerUserId;
        this.awayClubId = awayClubId;
        this.awayClubMangerUserId = awayClubMangerUserId;
        this.location = location;
        this.status = status;
        this.matchDateTime = matchDateTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public String getId() {
        return id;
    }

    public String getHomeClubId() {
        return homeClubId;
    }

    public String getHomeClubMangerUserId() {
        return homeClubMangerUserId;
    }

    public String getAwayClubMangerUserId() {
        return awayClubMangerUserId;
    }

    public String getAwayClubId() {
        return awayClubId;
    }

    public LocalDateTime getMatchDateTime() {
        return matchDateTime;
    }

    public String getLocation() {
        return location;
    }

    public MatchStatus getStatus() {
        return status;
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

    public void accept(final boolean accept, final String awayClubMangerUserId) {
        this.awayClubMangerUserId = awayClubMangerUserId;
        if (accept) {
            this.status = MatchStatus.ACCEPTED;
        } else {
            this.status = MatchStatus.CANCELLED;
        }
        this.addEvent(new AcceptedMatchEvent(id, homeClubId, homeClubMangerUserId, awayClubId, awayClubMangerUserId, location, matchDateTime));
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Match match = (Match) o;
        return Objects.equals(getId(), match.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public static Match init(
            final String id, final String homeClubId, final String homeClubMangerUserId,
            final String awayClubId, final LocalDateTime matchDateTime, final String location
    ) {
        final Match match = new Match(id, homeClubId, homeClubMangerUserId, awayClubId, null, location, PENDING, matchDateTime, LocalDateTime.now(), LocalDateTime.now(), null);
        match.addEvent(new InviteMatchEvent(id, homeClubId, awayClubId, matchDateTime, location));
        return match;
    }
}
