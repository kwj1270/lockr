package com.official.lockr.domain.game.game.application;

import com.official.lockr.domain.game.game.application.command.RegisterEntryGameCommand;
import com.official.lockr.domain.game.game.domain.Game;

public interface SubmitEntryGameUseCase {
    Game registerEntry(RegisterEntryGameCommand command);
}
