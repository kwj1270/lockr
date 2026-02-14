package com.official.lockr.domain.club.sport.football.squad.api.dto;

import com.official.lockr.domain.club.sport.football.squad.application.command.RegisterMySquadProfileCommand;
import com.official.lockr.global.vo.Foot;
import com.official.lockr.global.vo.Position;

import java.util.List;

public record RegisterMySquadProfileRequest(
        String height,
        String weight,
        String foot,
        List<String> positions,
        Integer backNumber
) {
    public RegisterMySquadProfileCommand toCommand(final String userId, final String clubId) {
        return new RegisterMySquadProfileCommand(
                userId,
                clubId,
                height,
                weight,
                Foot.valueOf(foot),
                positions.stream().map(Position::valueOf).toList(),
                backNumber
        );
    }
}
