package com.official.lockr.domain.shorts.application.usecase;

import com.official.lockr.domain.shorts.application.command.DeleteShortsCommand;

public interface DeleteShortsUseCase {

    void delete(DeleteShortsCommand command);
}
