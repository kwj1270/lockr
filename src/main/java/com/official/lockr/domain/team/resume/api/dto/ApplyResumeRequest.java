package com.official.lockr.domain.team.resume.api.dto;


import com.official.lockr.domain.team.resume.application.command.ApplyResumeCommand;

import java.util.List;

public record ApplyResumeRequest(
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
        String dominantFoot,
        String advantages,
        String disadvantages
) {
    public ApplyResumeCommand toCommand(final String teamId, final String userId) {
        return new ApplyResumeCommand(
                teamId, userId, birth, height, weight, name, email, address, phone, emergencyContactPhone,
                nationality, preferredPosition, dominantFoot, advantages, disadvantages
        );
    }
}
