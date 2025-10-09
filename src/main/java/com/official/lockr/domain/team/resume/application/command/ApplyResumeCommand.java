package com.official.lockr.domain.team.resume.application.command;

import java.util.List;

public record ApplyResumeCommand(
        String teamId,
        String userId,
        String birth,
        String height,
        String weight,
        String name,
        String email,
        String address,
        String phone,
        String emergencyContactPhone,
        String nationality,
        List<String> preferredPosition,
        String foot,
        String advantages,
        String disadvantages
) {
}
