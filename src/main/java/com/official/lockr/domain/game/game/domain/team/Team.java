package com.official.lockr.domain.game.game.domain.team;

import com.official.lockr.domain.game.game.domain.team.player.Bench;
import com.official.lockr.domain.game.game.domain.team.player.Field;
import com.official.lockr.domain.game.game.domain.team.player.Player;
import com.official.lockr.domain.game.game.domain.team.player.Entry;
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
    private Entry entry;
    private Score score;
    private Records records;

    public Team(final String id, final String teamId, final List<Player> entryPlayers, final Score score, final Records records) {
        this(id, teamId, new Entry(entryPlayers), score, records);
    }

    public Team(final String id, final String teamId,
                final Entry entry, final Score score, final Records records) {
        this.id = id;
        this.teamId = teamId;
        this.entry = entry;
        this.score = score;
        this.records = records;
    }

    public void registerEntry(final Field field, final Bench bench) {
        this.entry = new Entry(field, bench);
    }

    public void substitute(final String outPlayerId, final String inPlayerId, final int minute) {
        this.records = records.substitute(outPlayerId, inPlayerId, minute);
        entry.substitute(outPlayerId, inPlayerId, minute);
    }

    public void finishedGame(final int totalMinutes) {
        this.records = records.finalizePlayTime(totalMinutes);
    }

    public void yellowCard(final String playerId, final int minute) {
        entry.yellowCard(playerId, minute);
    }

    public void redCard(final String playerId, final int minute) {
        entry.redCard(playerId, minute);
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
        return entry.hasPlayerToBeSentOff(playerId);
    }

    public boolean isRegisteredEntry() {
        return entry.isRegistered();
    }

    public int scoreValue() {
        return score.value();
    }
}
