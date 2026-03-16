package com.official.lockr.domain.club.club.application.command;

public record UpdateMemberProfileImageCommand(
        String clubId,
        String userId,
        String profileImage
) {
}
