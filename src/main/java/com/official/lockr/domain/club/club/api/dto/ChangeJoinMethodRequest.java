package com.official.lockr.domain.club.club.api.dto;

import com.official.lockr.domain.club.club.application.command.ChangeJoinMethodCommand;

public record ChangeJoinMethodRequest(
        String joinMethod
) {

    public ChangeJoinMethodCommand toCommand(final String clubId, final String userId) {
        return new ChangeJoinMethodCommand(clubId, userId, joinMethod);
    }
}