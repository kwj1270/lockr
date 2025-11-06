package com.official.lockr.domain.club.schedule.domain.attendance;

public enum AttendanceStatus {
    ATTENDING("참석"),
    NOT_ATTENDING("불참"),
    TENTATIVE("미정"),
    NO_RESPONSE("응답 없음");

    private final String displayName;

    AttendanceStatus(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
