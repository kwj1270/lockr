package com.official.lockr.domain.club.schedule.domain;

public interface ScheduleCommentRepository {

    void save(String id, String scheduleId, String clubId, String userId, String content);
}
