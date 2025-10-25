package com.official.lockr.domain.game.match.domain;

import com.official.lockr.domain.game.match.domain.event.AcceptedMatchEvent;
import com.official.lockr.domain.game.match.domain.event.InviteMatchEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.domain.game.match.domain.MatchStatus.PENDING;

public class Match extends AggregateRoot {

    private final String id;
    private final String homeTeamId;
    private final String awayTeamId;
    private final LocalDateTime matchDateTime;
    private final String location;
    private MatchStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Match(final String id, final String homeTeamId, final String awayTeamId,
                 final LocalDateTime matchDateTime, final String location,
                 final MatchStatus status,
                 final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.matchDateTime = matchDateTime;
        this.location = location;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public String getId() {
        return id;
    }

    public String getHomeTeamId() {
        return homeTeamId;
    }

    public String getAwayTeamId() {
        return awayTeamId;
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

    public void accept(final boolean accept) {
        if (accept) {
            this.status = MatchStatus.ACCEPTED;
        } else {
            this.status = MatchStatus.CANCELLED;
        }
        this.addEvent(new AcceptedMatchEvent(id, homeTeamId, awayTeamId));
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
            final String id, final String homeTeamId, final String awayTeamId,
            final LocalDateTime matchDateTime, final String location
    ) {
        final Match match = new Match(id, homeTeamId, awayTeamId, matchDateTime, location, PENDING, LocalDateTime.now(), LocalDateTime.now(), null);
        match.addEvent(new InviteMatchEvent(match.id, homeTeamId, awayTeamId, matchDateTime, location));
        return match;
    }
}
