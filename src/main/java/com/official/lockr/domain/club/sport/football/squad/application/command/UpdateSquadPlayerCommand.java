package com.official.lockr.domain.club.sport.football.squad.application.command;

import com.official.lockr.global.vo.Foot;
import com.official.lockr.global.vo.Position;

import java.time.LocalDate;
import java.util.List;

public record UpdateSquadPlayerCommand(
        String userId,
        String clubId,
        String profileImage,
        String height,
        String weight,
        Foot foot,
        List<Position> positions,
        Integer backNumber
) {
}
