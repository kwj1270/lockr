package com.official.lockr.domain.game.game.domain;

import com.official.lockr.domain.game.game.domain.event.*;
import com.official.lockr.domain.relay.domain.RelayEventType;
import com.official.lockr.domain.game.game.domain.team.Team;
import com.official.lockr.domain.game.game.domain.team.player.Bench;
import com.official.lockr.domain.game.game.domain.team.player.Field;
import com.official.lockr.global.ddd.AggregateRoot;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;

public class Game extends AggregateRoot {

    private final String id;
    private final String invitationId;
    private final Team homeTeam;
    private final Team awayTeam;
    private GameStatus gameStatus;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Game(final String id,
                final String invitationId,
                final Team homeTeam,
                final Team awayTeam,
                final GameStatus gameStatus,
                final LocalDateTime startedAt,
                final LocalDateTime endedAt,
                final LocalDateTime createdAt,
                final LocalDateTime updatedAt,
                final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.invitationId = invitationId;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.gameStatus = gameStatus;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public void registerEntry(final String teamId, final Field field, final Bench bench) {
        final Team team = findTeam(teamId);
        team.registerEntry(field, bench);
    }

    public void start() {
        if (gameStatus != GameStatus.READY) {
            throw new IllegalStateException("Game already started");
        }
        this.gameStatus = GameStatus.FIRST_HALF;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        addEvent(new StartedGameEvent(this.id));
    }

    public void finish(final int totalMinutes) {
        if (gameStatus != GameStatus.SECOND_HALF) {
            throw new IllegalStateException("Game already finished");
        }

        homeTeam.finishedGame(totalMinutes);
        awayTeam.finishedGame(totalMinutes);

        this.gameStatus = GameStatus.FINISHED;
        this.endedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        addEvent(new FinishedGameEvent(this.id, homeTeam.scoreValue(), awayTeam.scoreValue(), totalMinutes));
    }

    public void goal(final String teamId, final String playerId, @Nullable final String relatedPlayerId, final int minute) {
        validateInProgress();

        final Team team = findTeam(teamId);
        team.goal();
        this.updatedAt = LocalDateTime.now();

        addEvent(new ScoredGoalEvent(id, teamId, RelayEventType.GOAL, playerId, relatedPlayerId, minute, homeTeam.score(), awayTeam.score()));
    }

    public void yellowCard(final String teamId, final String playerId, final int minute) {
        validateInProgress();

        final Team team = findTeam(teamId);
        team.yellowCard(playerId, minute);
        this.updatedAt = LocalDateTime.now();

        addEvent(new ReceivedYellowCardEvent(id, teamId, RelayEventType.YELLOW_CARD, playerId, minute));

        if (team.hasPlayerToBeSentOff(playerId)) {
            this.redCard(teamId, playerId, minute);
        }
    }

    public void redCard(final String teamId, final String playerId, final int minute) {
        validateInProgress();

        final Team team = findTeam(teamId);
        team.redCard(playerId, minute);
        this.updatedAt = LocalDateTime.now();

        addEvent(new ReceivedRedCardEvent(id, teamId, RelayEventType.YELLOW_CARD, playerId, minute));
    }

    public void substitutePlayer(final String teamId, final String outPlayerId, final String inPlayerId, final int minute) {
        validateInProgress();

        final Team team = findTeam(teamId);
        team.substitute(outPlayerId, inPlayerId, minute);
        this.updatedAt = LocalDateTime.now();

        addEvent(new SubstitutedPlayerEvent(this.id, teamId, outPlayerId, inPlayerId, minute));
    }

    private void validateInProgress() {
        if (gameStatus != GameStatus.FIRST_HALF && gameStatus != GameStatus.SECOND_HALF) {
            throw new IllegalStateException("Game is not in progress");
        }
    }

    @Nonnull
    private Team findTeam(final String teamId) {
        if (homeTeam.isTeam(teamId)) {
            return homeTeam;
        }
        if (awayTeam.isTeam(teamId)) {
            return awayTeam;
        }
        throw new IllegalArgumentException("Team not found: " + teamId);
    }

    public String getId() {
        return id;
    }

    public String getInvitationId() {
        return invitationId;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }


    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public boolean isNotReady() {
        return GameStatus.READY != gameStatus;
    }

    public boolean isNotSecondHalf() {
        return GameStatus.SECOND_HALF != gameStatus;
    }

    public boolean isNotRegisteredEntry() {
        return !(isRegisteredEntry());
    }

    public boolean isRegisteredEntry() {
        return homeTeam.isRegisteredEntry();
    }
}
