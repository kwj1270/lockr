package com.official.lockr.domain.club.contract.application.command;

import java.util.List;

public record ApplyResumeCommand(
        String clubId,
        String userId,
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
        String foot,
        String advantages,
        String disadvantages
) {
}
