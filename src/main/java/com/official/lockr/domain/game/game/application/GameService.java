package com.official.lockr.domain.game.game.application;

import com.official.lockr.domain.game.game.application.command.RegisterEntryGameCommand;
import com.official.lockr.domain.game.game.domain.Game;
import com.official.lockr.domain.game.game.domain.GameRepository;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
public class GameService implements SubmitEntryGameUseCase, StartGameUseCase, FinishGameUseCase {

    private final GameRepository gameRepository;

    public GameService(final GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public Game registerEntry(final RegisterEntryGameCommand command) {
        final Game game = gameRepository.find(command.gameId());
        if (isNull(game) || game.isNotReady()) {
            throw new IllegalArgumentException();
        }
        game.registerEntry(command.teamId(), command.fieldPlayers(), command.benchPlayers());
        return gameRepository.save(game);
    }

    @Override
    public Game start(final String gameId) {
        final Game game = gameRepository.find(gameId);
        if (isNull(game) || game.isNotReady() || game.isNotRegisteredEntry()) {
            throw new IllegalArgumentException();
        }
        game.start();
        return gameRepository.save(game);
    }

    @Override
    public Game finish(final String gameId, final int totalMinutes) {
        final Game game = gameRepository.find(gameId);
        if (isNull(game) || game.isNotSecondHalf()) {
            throw new IllegalArgumentException();
        }
        game.finish(totalMinutes);
        return gameRepository.save(game);
    }
}
