package com.official.lockr.domain.club.sport.football.squad.api.dto;

import com.official.lockr.domain.club.sport.football.squad.application.command.UpdateSquadPlayerCommand;
import com.official.lockr.global.vo.Foot;
import com.official.lockr.global.vo.Position;

import java.util.List;

public record UpdateSquadPlayerRequest(
        String profileImage,
        String height,
        String weight,
        String foot,
        List<String> positions,
        Integer backNumber
) {
    public UpdateSquadPlayerCommand toCommand(final String userId, final String clubId) {
        return new UpdateSquadPlayerCommand(
                userId,
                clubId,
                profileImage,
                height,
                weight,
                Foot.valueOf(foot),
                positions.stream().map(Position::valueOf).toList(),
                backNumber
        );
    }
}
