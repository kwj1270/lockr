package com.official.lockr.domain.club.club.api.dto;

import com.official.lockr.domain.club.club.application.command.DelegatePresidentCommand;

public record DelegatePresidentRequest(
        String targetUserId
) {

    public DelegatePresidentCommand toCommand(final String clubId, final String userId) {
        return new DelegatePresidentCommand(clubId, userId, targetUserId);
    }
}
