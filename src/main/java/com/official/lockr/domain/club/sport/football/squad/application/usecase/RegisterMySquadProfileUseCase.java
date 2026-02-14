package com.official.lockr.domain.club.sport.football.squad.application.usecase;

import com.official.lockr.domain.club.sport.football.squad.application.command.RegisterMySquadProfileCommand;
import com.official.lockr.domain.club.sport.football.squad.domain.Squad;

public interface RegisterMySquadProfileUseCase {
    Squad registerMyProfile(RegisterMySquadProfileCommand command);
}
