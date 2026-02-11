package com.official.lockr.domain.club.stats.domain;

public class PlayerPerformance {

    private final String id;
    private final String matchRecordId;
    private final String clubId;
    private final String userId;
    private final int goals;
    private final int assists;
    private final boolean isMom;
    private final Integer minutesPlayed;

    public PlayerPerformance(final String id, final String matchRecordId, final String clubId,
                             final String userId, final int goals, final int assists,
                             final boolean isMom, final Integer minutesPlayed) {
        if (goals < 0) {
            throw new IllegalArgumentException("Goals cannot be negative");
        }
        if (assists < 0) {
            throw new IllegalArgumentException("Assists cannot be negative");
        }
        this.id = id;
        this.matchRecordId = matchRecordId;
        this.clubId = clubId;
        this.userId = userId;
        this.goals = goals;
        this.assists = assists;
        this.isMom = isMom;
        this.minutesPlayed = minutesPlayed;
    }

    public String getId() {
        return id;
    }

    public String getMatchRecordId() {
        return matchRecordId;
    }

    public String getClubId() {
        return clubId;
    }

    public String getUserId() {
        return userId;
    }

    public int getGoals() {
        return goals;
    }

    public int getAssists() {
        return assists;
    }

    public boolean isMom() {
        return isMom;
    }

    public Integer getMinutesPlayed() {
        return minutesPlayed;
    }
}
