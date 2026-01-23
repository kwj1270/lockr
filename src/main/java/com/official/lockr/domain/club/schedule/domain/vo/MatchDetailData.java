package com.official.lockr.domain.club.schedule.domain.vo;

/**
 * 경기 상세 정보
 * @param homeClubId 홈 팀 클럽 ID (우리팀 또는 상대팀)
 * @param awayClubId 어웨이 팀 클럽 ID (우리팀 또는 상대팀)
 * @param opponentName 상대팀 이름 (등록되지 않은 클럽인 경우 필수)
 */
public record MatchDetailData(
        String homeClubId,
        String awayClubId,
        String opponentName
) implements ScheduleDetailData {
}
