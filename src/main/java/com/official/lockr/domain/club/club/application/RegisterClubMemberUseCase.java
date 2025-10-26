package com.official.lockr.domain.club.club.application;

import com.official.lockr.domain.club.club.application.command.AddMemberCommand;
import com.official.lockr.domain.club.club.domain.Club;

public interface RegisterClubMemberUseCase {
    Club addMember(AddMemberCommand command);
}
