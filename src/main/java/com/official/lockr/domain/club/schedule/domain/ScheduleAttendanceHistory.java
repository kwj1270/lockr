package com.official.lockr.domain.club.schedule.domain;

import com.official.lockr.global.util.UlidUtils;

import java.time.LocalDateTime;

public class ScheduleAttendanceHistory {

    private final String id;
    private final String scheduleId;
    private final String attendanceId;
    private final String userId;
    private final String changedBy;
    private final String changedByRole;
    private final AttendanceStatus previousStatus;
    private final AttendanceStatus newStatus;
    private final String reason;
    private final LocalDateTime changedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public static ScheduleAttendanceHistory create(
            final String scheduleId,
            final String attendanceId,
            final String userId,
            final String changedBy,
            final String changedByRole,
            final AttendanceStatus previousStatus,
            final AttendanceStatus newStatus,
            final String reason,
            final LocalDateTime changedAt
    ) {
        final LocalDateTime now = LocalDateTime.now();
        return new ScheduleAttendanceHistory(
                UlidUtils.generateUlid(),
                scheduleId,
                attendanceId,
                userId,
                changedBy,
                changedByRole,
                previousStatus,
                newStatus,
                reason,
                changedAt,
                now,
                now,
                null
        );
    }

    public ScheduleAttendanceHistory(
            final String id,
            final String scheduleId,
            final String attendanceId,
            final String userId,
            final String changedBy,
            final String changedByRole,
            final AttendanceStatus previousStatus,
            final AttendanceStatus newStatus,
            final String reason,
            final LocalDateTime changedAt,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt,
            final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.attendanceId = attendanceId;
        this.userId = userId;
        this.changedBy = changedBy;
        this.changedByRole = changedByRole;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedAt = changedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public String getId() {
        return id;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getAttendanceId() {
        return attendanceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public String getChangedByRole() {
        return changedByRole;
    }

    public AttendanceStatus getPreviousStatus() {
        return previousStatus;
    }

    public AttendanceStatus getNewStatus() {
        return newStatus;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
