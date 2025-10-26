package com.official.lockr.domain.relay.domain;

import static com.official.lockr.domain.relay.domain.RelayEventType.FINISH;
import static com.official.lockr.domain.relay.domain.RelayEventType.START;

public class RelayEvent {

    private final String id;
    private final String gameId;
    private final int phase;
    private final RelayEventType eventType;
    private final String clubId;
    private final String playerId;
    private final String playerName;
    private final String relatedPlayerId;
    private final String relatedPlayerName;
    private final int minute;
    private final Integer addMinute;
    private final int homeScore;
    private final int awayScore;

    public RelayEvent(final String id, final String gameId, final int phase,
                      final RelayEventType eventType, final String clubId, final String playerId,
                      final String playerName, final String relatedPlayerId, final String relatedPlayerName,
                      final int minute, final Integer addMinute, final int homeScore, final int awayScore
    ) {
        this.id = id;
        this.gameId = gameId;
        this.phase = phase;
        this.eventType = eventType;
        this.clubId = clubId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.relatedPlayerId = relatedPlayerId;
        this.relatedPlayerName = relatedPlayerName;
        this.minute = minute;
        this.addMinute = addMinute;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public static RelayEvent start(final String id, final String gameId) {
        return new RelayEvent(id, gameId, 1, START, null, null, null, null, null, 0, null, 0, 0);
    }

    public static RelayEvent finish(final String id, final String gameId, final int totalMinutes, final int homeScore, final int awayScore) {
        return new RelayEvent(id, gameId, 2, FINISH, null, null, null, null, null, 90, totalMinutes - 90, homeScore, awayScore);
    }

    public RelayEventType getEventType() {
        return eventType;
    }

    public String getClubId() {
        return clubId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getRelatedPlayerId() {
        return relatedPlayerId;
    }

    public int getMinute() {
        return minute;
    }
}
