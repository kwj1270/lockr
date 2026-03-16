package com.official.lockr.domain.club.schedule.infrastructure;

import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryScheduleRepository implements ScheduleRepository {

    private final Map<String, Schedule> store = new HashMap<>();

    @Override
    public Schedule findById(String id) {
        return store.get(id);
    }

    @Override
    public Schedule save(Schedule schedule) {
        store.put(schedule.getId(), schedule);
        return schedule;
    }

    public void clear() {
        store.clear();
    }
}
