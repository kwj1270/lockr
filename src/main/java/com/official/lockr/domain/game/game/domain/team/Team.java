package com.official.lockr.domain.game.game.domain.team;

import com.official.lockr.domain.game.game.domain.team.player.BenchPlayers;
import com.official.lockr.domain.game.game.domain.team.player.FieldPlayers;
import com.official.lockr.domain.game.game.domain.team.player.GamePlayer;
import com.official.lockr.domain.game.game.domain.team.player.Lineup;
import com.official.lockr.domain.game.game.domain.team.record.Records;
import com.official.lockr.domain.game.common.Score;

import java.util.List;

/**
 * 경기에 참여하는 팀
 * PlayerPlayRecords는 GameEvent로부터 파생된 집계 상태입니다.
 */
public class Team {

    private final String id;
    private final String teamId;
    private Lineup lineup;
    private Score score;
    private Records records;

    public Team(final String id, final String teamId, final List<GamePlayer> entryGamePlayers, final Score score, final Records records) {
        this(id, teamId, new Lineup(entryGamePlayers), score, records);
    }

    public Team(final String id, final String teamId,
                final Lineup lineup, final Score score, final Records records) {
        this.id = id;
        this.teamId = teamId;
        this.lineup = lineup;
        this.score = score;
        this.records = records;
    }

    public void registerEntry(final FieldPlayers fieldPlayers, final BenchPlayers benchPlayers) {
        this.lineup = new Lineup(fieldPlayers, benchPlayers);
    }

    public void substitute(final String outPlayerId, final String inPlayerId, final int minute) {
        this.records = records.substitute(outPlayerId, inPlayerId, minute);
        lineup.substitute(outPlayerId, inPlayerId, minute);
    }

    public void finishedGame(final int totalMinutes) {
        this.records = records.finalizePlayTime(totalMinutes);
    }

    public void yellowCard(final String playerId, final int minute) {
        lineup.yellowCard(playerId, minute);
    }

    public void redCard(final String playerId, final int minute) {
        lineup.redCard(playerId, minute);
    }

    public String getId() {
        return id;
    }

    public String getTeamId() {
        return teamId;
    }

    public boolean isTeam(final String teamId) {
        return this.teamId.equals(teamId);
    }

    public void goal() {
        this.score = score.addGoal();
    }

    public Score score() {
        return score;
    }

    public boolean hasPlayerToBeSentOff(final String playerId) {
        return lineup.hasPlayerToBeSentOff(playerId);
    }

    public boolean isRegisteredEntry() {
        return lineup.isRegistered();
    }

    public int scoreValue() {
        return score.value();
    }
}
