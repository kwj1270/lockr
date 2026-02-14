package com.official.lockr.domain.club.sport.football.squad.application.command;

import com.official.lockr.global.vo.Foot;
import com.official.lockr.global.vo.Position;

import java.util.List;

public record RegisterMySquadProfileCommand(
        String userId,
        String clubId,
        String height,
        String weight,
        Foot foot,
        List<Position> positions,
        Integer backNumber
) {
}
