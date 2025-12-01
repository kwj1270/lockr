package com.official.lockr.domain.club.recruitment.applications.application.command;

import java.util.Map;

public record SubmitTryoutCommand(
        String clubId,
        String userId,
        String recruitmentId,
        String applicationFormType,
        String name,
        String phone,
        String gender,
        String introduction,
        String profileImageUrl,
        String email,
        String address,
        String birthDate,
        String emergencyContactPhone,
        String sportType,
        Map<String, String> sportSpecificData
) {
}
