package com.official.lockr.domain.club.schedule.domain;

import com.official.lockr.domain.club.schedule.domain.attendance.Attendance;
import com.official.lockr.domain.club.schedule.domain.attendance.AttendanceStatus;
import com.official.lockr.domain.club.schedule.domain.detail.ScheduleDetail;
import com.official.lockr.domain.club.schedule.domain.event.CancelledScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.CreatedScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.RespondedToScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.UpdatedScheduleEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.official.lockr.domain.club.schedule.domain.ScheduleStatus.SCHEDULED;

public class Schedule extends AggregateRoot {

    private final String id;
    private final String clubId;
    private String title;
    private String content;
    private String location;
    private LocalDateTime scheduleTime;
    private final ScheduleType scheduleType;
    private ScheduleDetail detail;
    private final List<Attendance> attendances;
    private ScheduleStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Schedule create(
            final String id,
            final String clubId,
            final String title,
            final String content,
            final String scheduleLocation,
            final LocalDateTime scheduleTime,
            final ScheduleType scheduleType,
            final ScheduleDetail detail,
            final List<String> userIds
    ) {
        final LocalDateTime now = LocalDateTime.now();
        final Schedule schedule = new Schedule(
                id, clubId, title, content, scheduleLocation, scheduleTime, scheduleType,
                detail, createInitialAttendances(userIds), SCHEDULED, now, now, null
        );
        schedule.addEvent(new CreatedScheduleEvent(id, clubId, scheduleType, scheduleTime));
        return schedule;
    }

    public Schedule(
            final String id,
            final String clubId,
            final String title,
            final String content,
            final String location,
            final LocalDateTime scheduleTime,
            final ScheduleType scheduleType,
            final ScheduleDetail detail,
            final List<Attendance> attendances,
            final ScheduleStatus status,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt,
            final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.clubId = clubId;
        this.title = title;
        this.content = content;
        this.location = location;
        this.scheduleTime = scheduleTime;
        this.scheduleType = scheduleType;
        this.detail = detail;
        this.attendances = new ArrayList<>(attendances);  // 방어적 복사
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public void respond(final String userId, final AttendanceStatus status) {
        validateNotCancelled();

        final Attendance attendance = findAttendance(userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not invited: " + userId));

        final AttendanceStatus previousStatus = attendance.getStatus();
        attendance.updateStatus(status);

        this.updatedAt = LocalDateTime.now();
        addEvent(new RespondedToScheduleEvent(id, clubId, userId, previousStatus, status));
    }

    public void respondWithReason(final String userId, final AttendanceStatus status, final String reason) {
        validateNotCancelled();

        final Attendance attendance = findAttendance(userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not invited: " + userId));

        final AttendanceStatus previousStatus = attendance.getStatus();
        attendance.updateStatusWithReason(status, reason);

        this.updatedAt = LocalDateTime.now();
        addEvent(new RespondedToScheduleEvent(id, clubId, userId, previousStatus, status));
    }

    public void update(
            final String title,
            final String content,
            final String scheduleLocation,
            final LocalDateTime scheduleTime,
            final ScheduleDetail detail
    ) {
        validateNotCancelled();
        validateScheduleTime(scheduleTime);

        this.title = title;
        this.content = content;
        this.location = scheduleLocation;
        this.scheduleTime = scheduleTime;
        this.detail = detail;
        this.updatedAt = LocalDateTime.now();

        addEvent(new UpdatedScheduleEvent(id, clubId, scheduleTime));
    }

    public void cancel() {
        validateNotCancelled();

        this.status = ScheduleStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        this.deletedAt = LocalDateTime.now();

        addEvent(new CancelledScheduleEvent(id, clubId, scheduleType));
    }

    // 도메인 로직: 멤버 추가
    public void addMember(final String userId) {
        validateNotCancelled();

        if (isInvited(userId)) {
            throw new IllegalArgumentException("User already invited: " + userId);
        }

        attendances.add(Attendance.create(userId));
        this.updatedAt = LocalDateTime.now();
    }

    // 조회 메서드들
    public boolean isInvited(final String userId) {
        return attendances.stream()
                .anyMatch(a -> a.getUserId().equals(userId));
    }

    public boolean isAttending(final String userId) {
        return findAttendance(userId)
                .map(Attendance::isAttending)
                .orElse(false);
    }

    public int getAttendingCount() {
        return (int) attendances.stream()
                .filter(Attendance::isAttending)
                .count();
    }

    public int getNotAttendingCount() {
        return (int) attendances.stream()
                .filter(Attendance::isNotAttending)
                .count();
    }

    public int getNoResponseCount() {
        return (int) attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.NO_RESPONSE)
                .count();
    }

    public boolean isCancelled() {
        return status == ScheduleStatus.CANCELLED;
    }

    public boolean isPast() {
        return scheduleTime.isBefore(LocalDateTime.now());
    }

    // 내부 헬퍼 메서드
    private Optional<Attendance> findAttendance(final String userId) {
        return attendances.stream()
                .filter(a -> a.getUserId().equals(userId))
                .findFirst();
    }

    private static List<Attendance> createInitialAttendances(final List<String> userIds) {
        return userIds.stream()
                .map(Attendance::create)
                .toList();
    }

    // 유효성 검증
    private static void validateScheduleTime(final LocalDateTime scheduleTime) {
        if (scheduleTime == null) {
            throw new IllegalArgumentException("Schedule time is required");
        }
        if (scheduleTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Schedule time must be in the future");
        }
    }


    private void validateNotCancelled() {
        if (isCancelled()) {
            throw new IllegalStateException("Cannot modify cancelled schedule");
        }
    }

    // Getter들 (불변성 보장)
    public String getId() {
        return id;
    }

    public String getClubId() {
        return clubId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getScheduleTime() {
        return scheduleTime;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public ScheduleDetail getDetail() {
        return detail;
    }

    public List<Attendance> getAttendances() {
        return List.copyOf(attendances);  // 불변 복사본 반환
    }

    public ScheduleStatus getStatus() {
        return status;
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
