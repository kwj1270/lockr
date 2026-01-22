package com.official.lockr.domain.club.club.api.dto;

import com.official.lockr.domain.club.club.application.command.UpdateMemberProfileImageCommand;

public record UpdateMemberProfileImageRequest(
        String profileImage
) {
    public UpdateMemberProfileImageCommand toCommand(final String clubId, final String userId) {
        return new UpdateMemberProfileImageCommand(clubId, userId, profileImage);
    }
}
