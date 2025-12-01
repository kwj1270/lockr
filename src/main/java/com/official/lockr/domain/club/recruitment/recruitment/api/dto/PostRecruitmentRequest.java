package com.official.lockr.domain.club.recruitment.recruitment.api.dto;

import com.official.lockr.domain.club.recruitment.recruitment.application.command.PostRecruitmentCommand;

import java.time.LocalDateTime;
import java.util.List;

public record PostRecruitmentRequest(
        boolean isPublic,
        String title,
        String content,
        String applicationType,
        LocalDateTime recruitmentStart,
        LocalDateTime recruitmentEnd,
        String activityRegion,
        List<String> activityDays,
        String activityTime,
        int monthlyFee,
        String contactMethod
) {
    public PostRecruitmentCommand toCommand(final String clubId, final String userId) {
        return new PostRecruitmentCommand(clubId, userId, isPublic, title, content,
                applicationType, recruitmentStart, recruitmentEnd, activityRegion,
                activityDays, activityTime, monthlyFee, contactMethod);
    }
}
