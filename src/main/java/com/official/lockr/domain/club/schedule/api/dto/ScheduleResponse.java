package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.vo.ScheduleDetailData;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleResponse(
        String id,
        String clubId,
        String title,
        String content,
        String location,
        LocalDateTime scheduleTime,
        ScheduleType scheduleType,
        ScheduleDetailData detail,
        List<AttendanceResponse> attendances,
        ScheduleStatus status,
        Integer minParticipants,
        int deadlineDays,
        int attendingCount,
        int notAttendingCount,
        int noResponseCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ScheduleResponse from(final Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getClubId(),
                schedule.getTitle(),
                schedule.getContent(),
                schedule.getLocation(),
                schedule.getScheduleTime(),
                schedule.getScheduleType(),
                schedule.getDetail(),
                schedule.getAttendances().stream()
                        .map(AttendanceResponse::from)
                        .toList(),
                schedule.getStatus(),
                schedule.getMinParticipants(),
                schedule.getDeadlineDays(),
                schedule.getAttendingCount(),
                schedule.getNotAttendingCount(),
                schedule.getNoResponseCount(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}
