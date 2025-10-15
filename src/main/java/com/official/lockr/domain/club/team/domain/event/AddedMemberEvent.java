package com.official.lockr.domain.club.team.domain.event;

import com.official.lockr.domain.club.team.domain.Member;
import com.official.lockr.domain.club.team.domain.MemberRole;
import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record AddedMemberEvent(
        String id,
        String userId,
        MemberRole role,
        String teamId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) implements DomainEvent {
    public AddedMemberEvent(final Member member) {
        this(
                member.getId(), member.getUserId(), member.getRole(), member.getTeamId(),
                member.getCreatedAt(), member.getUpdatedAt(), member.getDeletedAt()
        );
    }
}
