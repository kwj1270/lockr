package com.official.lockr.domain.club.recruitment.applications.domain.vo;

import java.time.LocalDateTime;

public record ProcessingInfo(
        String processedByUserId,
        LocalDateTime processedAt,
        String reason
) {

}
