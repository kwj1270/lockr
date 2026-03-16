package com.official.lockr.domain.club.feed.domain;

public enum FeedType {
    GENERAL,    // 일반 게시물 - 모든 멤버 작성 가능
    NOTICE;     // 공지사항 - 운영진(COACH 이상)만 작성 가능

    public boolean requiresStaffPermission() {
        return this == NOTICE;
    }
}
