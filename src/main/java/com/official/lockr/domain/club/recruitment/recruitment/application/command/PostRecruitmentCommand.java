package com.official.lockr.domain.club.recruitment.recruitment.application.command;

import java.time.LocalDateTime;
import java.util.List;

public record PostRecruitmentCommand(
        String clubId,
        String userId,
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
}
