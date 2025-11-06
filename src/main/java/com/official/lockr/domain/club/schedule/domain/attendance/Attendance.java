package com.official.lockr.domain.club.schedule.domain.attendance;

import com.official.lockr.global.util.UlidUtils;

import java.time.LocalDateTime;

public class Attendance {

    private final String id;
    private final String userId;
    private AttendanceStatus status;
    private String reason;
    private LocalDateTime respondedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 팩토리 메서드: 초기 생성
    public static Attendance create(final String userId) {
        final String id = UlidUtils.generateUlid();
        final LocalDateTime now = LocalDateTime.now();
        return new Attendance(id, userId, AttendanceStatus.NO_RESPONSE, null, null, now, now);
    }

    // 전체 생성자 (Repository용)
    public Attendance(final String id, final String userId,
                      final AttendanceStatus status, final String reason, final LocalDateTime respondedAt,
                      final LocalDateTime createdAt, final LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.reason = reason;
        this.respondedAt = respondedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 도메인 로직: 참석 여부 응답
    public void updateStatus(final AttendanceStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = newStatus;
        this.respondedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 도메인 로직: 사유와 함께 응답
    public void updateStatusWithReason(final AttendanceStatus newStatus, final String reason) {
        updateStatus(newStatus);
        this.reason = reason;
    }

    // 조회 메서드
    public boolean hasResponded() {
        return status != AttendanceStatus.NO_RESPONSE;
    }

    public boolean isAttending() {
        return status == AttendanceStatus.ATTENDING;
    }

    public boolean isNotAttending() {
        return status == AttendanceStatus.NOT_ATTENDING;
    }

    // Getter들
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
