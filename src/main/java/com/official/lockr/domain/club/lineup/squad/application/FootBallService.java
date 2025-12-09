package com.official.lockr.domain.club.lineup.squad.application;

import com.official.lockr.domain.club.lineup.squad.application.dto.AddFootBallPlayerCommand;
import com.official.lockr.domain.club.lineup.squad.application.usecase.AddFootBallPlayerUseCase;
import com.official.lockr.domain.club.lineup.squad.domain.*;
import com.official.lockr.domain.club.recruitment.applications.domain.Application;
import com.official.lockr.domain.club.recruitment.applications.domain.ApplicationRepository;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.sport.FootballSportSpecificData;
import com.official.lockr.global.vo.BackNumber;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

import static com.github.f4b6a3.ulid.UlidCreator.getUlid;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class FootBallService implements AddFootBallPlayerUseCase {

    private final ApplicationRepository applicationRepository;
    private final SquadRepository squadRepository;

    public FootBallService(final ApplicationRepository applicationRepository,
                           final SquadRepository squadRepository) {
        this.applicationRepository = applicationRepository;
        this.squadRepository = squadRepository;
    }

    @Override
    public Squad addPlayer(final AddFootBallPlayerCommand command) {
        final Squad lineUp = squad(command.clubId());
        if (lineUp.hasPlayer(command.userId())) {
            return lineUp;
        }
        final BackNumber backNumber = new BackNumber(randomNumber());
        final Application application = applicationRepository.findByClubAndUser(command.clubId(), command.userId());
        final SquadPlayer lineUpMember = squadMemer(lineUp.getId(), command.userId(), application, backNumber);
        lineUp.addPlayer(lineUpMember);
        return squadRepository.save(lineUp);
    }

    private Squad squad(final String clubId) {
        final Squad lineUp = squadRepository.findByClubId(clubId);
        if (nonNull(lineUp)) {
            return lineUp;
        }
        return new Squad(getUlid().toString(), clubId, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    private SquadPlayer squadMemer(final String squadId, String memberId, final Application application, final BackNumber backNumber) {
        if (isNull(application)) {
            return SquadPlayer.init(getUlid().toString(), squadId, memberId, backNumber);
        }
        final FootballSportSpecificData footballSportSpecificData = (FootballSportSpecificData) application.getSportSpecificData();
        return SquadPlayer.init(
                getUlid().toString(),
                squadId,
                memberId,
                application,
                footballSportSpecificData,
                backNumber
        );
    }

    private static int randomNumber() {
        return new Random().nextInt(99);
    }
}
