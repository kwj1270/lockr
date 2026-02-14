package com.official.lockr.domain.club.club.application.command;

import com.official.lockr.domain.club.club.domain.MemberRole;
import reactor.util.annotation.Nullable;

public record AddMemberCommand(
        String clubId,
        String userId,
        MemberRole memberRole,
        @Nullable String name,
        @Nullable String profileImage
) {
    public static AddMemberCommand president(final String clubId, final String userId, @Nullable final String name, @Nullable final String profileImage) {
        return new AddMemberCommand(clubId, userId, MemberRole.PRESIDENT, name, profileImage);
    }

    public static AddMemberCommand basic(final String clubId, final String userId, @Nullable final String name, @Nullable final String profileImage) {
        return new AddMemberCommand(clubId, userId, MemberRole.BASIC, name, profileImage);
    }
}
