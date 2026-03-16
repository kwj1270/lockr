package com.official.lockr.domain.club.contract.api.dto;


import com.official.lockr.domain.club.contract.application.command.ApplyResumeCommand;

import java.util.List;

public record ApplyResumeRequest(
        String profileImage,
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
    public ApplyResumeCommand toCommand(final String clubId, final String userId) {
        return new ApplyResumeCommand(
                clubId, userId, profileImage, birth, height, weight, name, email, address, phone, emergencyContactPhone,
                nationality, preferredPosition, dominantFoot, advantages, disadvantages
        );
    }
}
