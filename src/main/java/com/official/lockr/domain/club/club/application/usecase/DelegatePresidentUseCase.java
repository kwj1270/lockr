package com.official.lockr.domain.club.club.application.usecase;

import com.official.lockr.domain.club.club.application.command.DelegatePresidentCommand;
import com.official.lockr.domain.club.club.domain.Club;

public interface DelegatePresidentUseCase {
    Club delegatePresident(DelegatePresidentCommand command);
}
