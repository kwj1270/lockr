package com.official.lockr.domain.game.game.domain.team.record;

import java.util.HashMap;
import java.util.Map;

/**
 * PlayerPlayRecord 집합을 관리하고 GameEvent로부터 복원하는 일급 컬렉션
 */
public class Records {

    private final Map<String, Record> records;

    private Records(final Map<String, Record> records) {
        this.records = new HashMap<>(records);
    }

    public Records substitute(final String outPlayerId, final String inPlayerId, final int minute) {
        final Map<String, Record> newRecords = new HashMap<>(this.records);

        final Record outRecord = findRecord(outPlayerId);
        final Record inRecord = findRecord(inPlayerId);

        newRecords.put(outPlayerId, outRecord.substituteOut(minute));
        newRecords.put(inPlayerId, inRecord.substituteIn(minute));

        return new Records(newRecords);
    }

    public Records finalizePlayTime(final int totalMinutes) {
        final Map<String, Record> newRecords = new HashMap<>();

        records.forEach((playerId, record) -> {
            if (record.isCurrentlyPlaying()) {
                newRecords.put(playerId, record.end(totalMinutes));
            } else {
                newRecords.put(playerId, record);
            }
        });

        return new Records(newRecords);
    }

    private Record findRecord(final String playerId) {
        final Record record = records.get(playerId);
        if (record == null) {
            throw new IllegalArgumentException("Player not found: " + playerId);
        }
        return record;
    }
}
