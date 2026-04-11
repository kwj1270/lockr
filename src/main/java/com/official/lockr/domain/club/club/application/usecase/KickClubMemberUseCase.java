package com.official.lockr.domain.club.club.application.usecase;

import com.official.lockr.domain.club.club.application.command.KickClubMemberCommand;

public interface KickClubMemberUseCase {
    void kick(KickClubMemberCommand command);
}
