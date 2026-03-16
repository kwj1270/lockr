package com.official.lockr.domain.club.recruitment.applications.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record RejectedApplicationEvent(
        String id,
        String clubId,
        String recruitmentId,
        String userId,
        String applicationStatus,
        String processedByUserId,
        String reason
) implements DomainEvent {

}
