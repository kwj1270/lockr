package com.official.lockr.domain.club.recruitment.recruitment.api.dto;

import com.official.lockr.domain.club.recruitment.recruitment.application.command.PostRecruitmentCommand;

import java.util.List;

public record PostRecruitmentRequest(
        boolean isPublic,
        String title,
        String content,
        String applicationType,
        String activityCity,
        String activityDistrict,
        List<String> activityDays,
        String activityTime,
        int monthlyFee,
        String contactMethod
) {
    public PostRecruitmentCommand toCommand(final String clubId, final String userId) {
        return new PostRecruitmentCommand(clubId, userId, isPublic, title, content,
                applicationType, activityCity, activityDistrict, activityDays, activityTime, monthlyFee, contactMethod);
    }
}
