package com.official.lockr.domain.game.match.application;

import com.official.lockr.domain.game.match.application.command.AcceptMatchCommand;
import com.official.lockr.domain.game.match.application.command.InviteMatchCommand;
import com.official.lockr.domain.game.match.domain.Match;
import com.official.lockr.domain.game.match.domain.MatchRepository;
import com.official.lockr.domain.game.match.domain.Manager;
import com.official.lockr.domain.game.match.domain.Managers;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.util.Objects.isNull;

@Service
public class MatchService implements InviteMatchUseCase, AcceptMatchUseCase {

    private final Managers managers;
    private final MatchRepository matchRepository;

    public MatchService(final Managers managers,
                        final MatchRepository matchRepository) {
        this.managers = managers;
        this.matchRepository = matchRepository;
    }

    @Override
    public Match invite(final InviteMatchCommand command) {
        final Manager manager = managers.find(command.homeTeamId(), command.homeProposeUserId());
        if (manager.isNotAuthorized()) {
            throw new IllegalStateException();
        }
        return matchRepository.save(Match.init(UUID.randomUUID().toString(), command.homeTeamId(), command.awayTeamId(), command.matchDateTime(), command.location()));
    }

    @Override
    public Match accept(final AcceptMatchCommand command) {
        final Manager manager = managers.find(command.userId(), command.teamId());
        if (manager.isNotAuthorized()) {
            throw new IllegalStateException();
        }
        final Match match = matchRepository.find(command.matchProposeId());
        if (isNull(match)) {
            throw new IllegalArgumentException();
        }
        match.accept(command.accept());
        return matchRepository.save(match);
    }
}
