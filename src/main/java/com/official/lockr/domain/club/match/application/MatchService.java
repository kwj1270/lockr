package com.official.lockr.domain.club.match.application;

import com.official.lockr.domain.club.match.application.command.AcceptMatchCommand;
import com.official.lockr.domain.club.match.application.command.InviteMatchCommand;
import com.official.lockr.domain.club.match.domain.ClubManager;
import com.official.lockr.domain.club.match.domain.ClubManagers;
import com.official.lockr.domain.club.match.domain.Match;
import com.official.lockr.domain.club.match.domain.MatchRepository;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class MatchService implements InviteMatchUseCase, AcceptMatchUseCase {

    private final ClubManagers clubManagers;
    private final MatchRepository matchRepository;

    public MatchService(final ClubManagers clubManagers,
                        final MatchRepository matchRepository) {
        this.clubManagers = clubManagers;
        this.matchRepository = matchRepository;
    }

    @Override
    public Match invite(final InviteMatchCommand command) {
        final ClubManager clubManager = clubManager(command.homeClubId(), command.homeClubManagerUserId());
        final Match match = Match.init(generateUlid(), command.homeClubId(), clubManager.getUserId(), command.awayClubId(), command.matchDateTime(), command.location());
        return matchRepository.save(match);
    }

    @Override
    public Match accept(final AcceptMatchCommand command) {
        final ClubManager clubManager = clubManager(command.userId(), command.clubId());
        final Match match = matchRepository.find(command.matchProposeId());
        if (isNull(match)) {
            throw new IllegalArgumentException();
        }
        match.accept(command.accept(), clubManager.getUserId());
        return matchRepository.save(match);
    }

    @Nonnull
    private ClubManager clubManager(final String clubId, final String clubManagerUserId) {
        final ClubManager clubManager = clubManagers.find(clubId, clubManagerUserId);
        if (isNull(clubManager)) {
            throw new IllegalStateException();
        }
        return clubManager;
    }
}
