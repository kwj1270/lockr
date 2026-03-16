package com.official.lockr.domain.club.club.api.dto;

import com.official.lockr.domain.club.club.application.command.ChangeMemberRoleCommand;
import com.official.lockr.domain.club.club.domain.MemberRole;

public record ChangeMemberRoleRequest(
        MemberRole role
) {

    public ChangeMemberRoleCommand toCommand(final String clubId, final String userId, final String memberId) {
        return new ChangeMemberRoleCommand(clubId, userId, memberId, role);
    }
}
