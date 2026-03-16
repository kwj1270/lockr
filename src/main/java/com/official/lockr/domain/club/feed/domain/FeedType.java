package com.official.lockr.domain.club.feed.domain;

public enum FeedType {
    GENERAL,    // 일반 게시물 - 모든 멤버 작성 가능
    NOTICE,     // 공지사항 - 운영진(COACH 이상)만 작성 가능
    SCHEDULE;   // 일정 피드 - 시스템 자동 생성, 수동 수정/삭제 불가

    public boolean requiresStaffPermission() {
        return this == NOTICE;
    }

    public boolean isSystemManaged() {
        return this == SCHEDULE;
    }
}
