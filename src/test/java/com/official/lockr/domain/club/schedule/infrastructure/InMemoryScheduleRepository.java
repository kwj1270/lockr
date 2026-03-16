package com.official.lockr.domain.club.schedule.infrastructure;

import com.official.lockr.domain.club.schedule.domain.Attendance;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryScheduleRepository implements ScheduleRepository {

    private final Map<String, Schedule> store = new HashMap<>();

    @Override
    public Schedule findById(String id) {
        final Schedule schedule = store.get(id);
        if (schedule == null) {
            return null;
        }
        return deepCopy(schedule);
    }

    @Override
    public Schedule save(Schedule schedule) {
        store.put(schedule.getId(), deepCopy(schedule));
        return schedule;
    }

    public void clear() {
        store.clear();
    }

    private static Schedule deepCopy(Schedule schedule) {
        return Schedule.reconstruct(
                schedule.getId(),
                schedule.getClubId(),
                schedule.getTitle(),
                schedule.getContent(),
                schedule.getLocation(),
                schedule.getScheduleTime(),
                schedule.getScheduleType(),
                schedule.getDetail(),
                schedule.getAttendances().stream()
                        .map(a -> new Attendance(
                                a.getId(), a.getUserId(), a.getStatus(), a.getReason(),
                                a.getCreatedAt(), a.getUpdatedAt(), a.getDeletedAt()
                        ))
                        .toList(),
                schedule.getStatus(),
                schedule.getMinParticipants(),
                schedule.getDeadlineDays(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt(),
                schedule.getDeletedAt()
        );
    }
}
