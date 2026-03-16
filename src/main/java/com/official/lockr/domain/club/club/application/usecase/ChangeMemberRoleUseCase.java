package com.official.lockr.domain.club.club.application.usecase;

import com.official.lockr.domain.club.club.application.command.ChangeMemberRoleCommand;
import com.official.lockr.domain.club.club.domain.Club;

public interface ChangeMemberRoleUseCase {
    Club changeMemberRole(ChangeMemberRoleCommand command);
}
