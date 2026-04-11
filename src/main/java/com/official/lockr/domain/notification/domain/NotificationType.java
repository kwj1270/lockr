package com.official.lockr.domain.notification.domain;

public enum NotificationType {
    SCHEDULE_LINK_REQUEST,  // 일정 링크 요청
    SCHEDULE_UPDATED,       // 일정 업데이트
    SCHEDULE_CANCELLED,     // 일정 취소
    FEE_UNPAID_REMINDER     // 회비 미납 알림
}
