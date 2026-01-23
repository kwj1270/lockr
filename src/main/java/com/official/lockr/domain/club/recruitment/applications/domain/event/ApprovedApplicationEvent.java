package com.official.lockr.domain.club.recruitment.applications.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record ApprovedApplicationEvent(
        String id,
        String clubId,
        String recruitmentId,
        String userId,
        String sportType,
        String applicationStatus,
        String processedByUserId,
        String profileImage
) implements DomainEvent {

}
