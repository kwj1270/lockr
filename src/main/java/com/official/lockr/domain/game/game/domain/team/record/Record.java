package com.official.lockr.domain.game.game.domain.team.record;

import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * 선수의 경기 출전 기록 (불변 객체)
 * GameEvent로부터 파생된 집계 상태를 나타냅니다.
 */
public class Record {

    private final String playerId;
    private final Integer startMinute;
    private final Integer endMinute;

    private Record(final String playerId,
                   final Integer startMinute,
                   final Integer endMinute
    ) {
        this.playerId = playerId;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    public static Record start(final String playerId) {
        return new Record(playerId, 0, null);
    }

    public Record end(final int minute) {
        return new Record(this.playerId, this.startMinute, minute);
    }

    public Record substituteIn(final int minute) {
        return new Record(this.playerId, minute, null);
    }

    public Record substituteOut(final int minute) {
        return new Record(this.playerId, this.startMinute, minute);
    }

    public boolean isCurrentlyPlaying() {
        return nonNull(startMinute) && isNull(endMinute);
    }

    public String getPlayerId() {
        return playerId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Record that = (Record) o;
        return Objects.equals(getPlayerId(), that.getPlayerId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPlayerId());
    }
}
