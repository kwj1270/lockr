package com.official.lockr.domain.club.recruitment.recruitment.application.command;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateRecruitmentCommand(
        String clubId,
        String recruitmentId,
        String userId,
        boolean isPublic,
        String title,
        String content,
        String status,
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
