package com.official.lockr.domain.club.club.application.command;

import com.official.lockr.domain.club.club.domain.MemberRole;

public record ChangeMemberRoleCommand(
        String clubId,
        String userId,
        String targetMemberId,
        MemberRole role
) {
}
