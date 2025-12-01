package com.official.lockr.domain.club.recruitment.recruitment.api.dto;

import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;
import com.official.lockr.domain.club.recruitment.recruitment.domain.vo.RecruitmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record RecruitmentResponse(
        String id,
        String clubId,
        RecruitmentStatus status,
        boolean isPublic,
        String title,
        String content,
        String activityRegion,
        List<String> activityDays,
        String activityTime,
        int monthlyFee,
        String contactMethod,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RecruitmentResponse from(final Recruitment recruitment) {
        return new RecruitmentResponse(
                recruitment.getId(),
                recruitment.getClubId(),
                recruitment.getStatus(),
                recruitment.isPublic(),
                recruitment.getTitle(),
                recruitment.getContent(),
                recruitment.getRegion(),
                recruitment.getActivityDays(),
                recruitment.getActivityTime(),
                recruitment.getMonthlyFee(),
                recruitment.getContactMethod(),
                recruitment.getCreatedAt(),
                recruitment.getUpdatedAt()
        );
    }
}
