package com.official.lockr.domain.club.recruitment.recruitment.api.dto;

import com.official.lockr.domain.club.recruitment.recruitment.application.command.UpdateRecruitmentCommand;

import java.util.List;

public record UpdateRecruitmentRequest(
        boolean isPublic,
        String title,
        String content,
        String status,
        String applicationType,
        String activityCity,
        String activityDistrict,
        List<String> activityDays,
        String activityTime,
        int monthlyFee,
        String contactMethod
) {
    public UpdateRecruitmentCommand toCommand(final String clubId, final String recruitmentId, final String userId) {
        return new UpdateRecruitmentCommand(clubId, recruitmentId, userId, isPublic, title, content, status, applicationType,
                activityCity, activityDistrict, activityDays, activityTime, monthlyFee, contactMethod);
    }
}
