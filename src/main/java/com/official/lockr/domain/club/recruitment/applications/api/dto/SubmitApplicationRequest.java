package com.official.lockr.domain.club.recruitment.applications.api.dto;

import com.official.lockr.domain.club.recruitment.applications.application.command.SubmitTryoutCommand;

import java.util.Map;

public record SubmitApplicationRequest(
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
    public SubmitTryoutCommand toCommand(final String clubId, final String userId) {
        return new SubmitTryoutCommand(
                clubId,
                userId,
                recruitmentId,
                applicationFormType,
                name,
                phone,
                email,
                emergencyContactPhone,
                profileImageUrl,
                birthDate,
                gender,
                address,
                introduction,
                sportType,
                sportSpecificData
        );
    }
}
