package com.official.lockr.domain.club.match.domain;

public enum MatchStatus {
    PENDING,      // 매칭 대기 (한 팀만 요청)
    ACCEPTED,     // 수락됨 (양 팀 모두 동의)
    CANCELLED     // 취소됨
}
