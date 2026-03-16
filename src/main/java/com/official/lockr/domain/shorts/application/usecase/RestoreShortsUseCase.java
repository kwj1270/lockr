package com.official.lockr.domain.shorts.application.usecase;

import com.official.lockr.domain.shorts.application.command.RestoreShortsCommand;

public interface RestoreShortsUseCase {

    void restore(RestoreShortsCommand command);
}
