package com.official.lockr.domain.club.club.domain.event;

import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.club.domain.MemberRole;
import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record AddedClubMemberEvent(
        String id,
        String userId,
        MemberRole role,
        String clubId,
        String sportType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) implements DomainEvent {
    public AddedClubMemberEvent(final Member member, final String sportType) {
        this(
                member.getId(), member.getUserId(), member.getRole(), member.getClubId(),
                sportType,
                member.getCreatedAt(), member.getUpdatedAt(), member.getDeletedAt()
        );
    }
}
