package com.official.lockr.domain.club.recruitment.recruitment.application.command;

import java.util.List;

public record PostRecruitmentCommand(
        String clubId,
        String userId,
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
}
