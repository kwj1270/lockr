package com.official.lockr.domain.game.game.domain.team.record;

public enum RecordType {
    PLAYING,            // 현재 출전 중
    SUBSTITUTED_OUT,    // 교체 아웃됨
    ON_BENCH,           // 벤치 (아직 투입 안됨)
    SENDING_OFF,        // 퇴장
    ON_RESERVE          // 경기 미출전 엔트리 선수
}
