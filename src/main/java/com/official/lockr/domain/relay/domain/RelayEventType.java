package com.official.lockr.domain.relay.domain;

public enum RelayEventType {
    START,          // 경기 시작
    GOAL,           // 골
    ASSIST,         // 어시스트
    YELLOW_CARD,    // 옐로우 카드
    RED_CARD,       // 레드 카드
    SUBSTITUTION,   // 선수 교체
    FINISH          // 경기 종료
}
