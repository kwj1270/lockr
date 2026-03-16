package com.official.lockr.domain.club.sport.football.squad.application;

import com.official.lockr.domain.club.recruitment.applications.domain.Application;
import com.official.lockr.domain.club.recruitment.applications.domain.ApplicationRepository;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.sport.FootballSportSpecificData;
import com.official.lockr.domain.club.sport.football.squad.application.command.AddFootBallPlayerCommand;
import com.official.lockr.domain.club.sport.football.squad.application.command.CreateSquadCommand;
import com.official.lockr.domain.club.sport.football.squad.application.command.RegisterMySquadProfileCommand;
import com.official.lockr.domain.club.sport.football.squad.application.command.UpdateSquadPlayerCommand;
import com.official.lockr.domain.club.sport.football.squad.application.command.RemoveSquadPlayerCommand;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.AddSquadPlayerUseCase;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.CreateSquadUseCase;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.RegisterMySquadProfileUseCase;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.RemoveSquadPlayerUseCase;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.UpdateSquadPlayerUseCase;
import com.official.lockr.domain.club.sport.football.squad.domain.Squad;
import com.official.lockr.domain.club.sport.football.squad.domain.SquadPlayer;
import com.official.lockr.domain.club.sport.football.squad.domain.SquadRepository;
import com.official.lockr.domain.users.domain.Users;
import com.official.lockr.domain.users.domain.UsersRepository;
import com.official.lockr.global.vo.BackNumber;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

import static com.github.f4b6a3.ulid.UlidCreator.getUlid;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class SquadService implements CreateSquadUseCase, AddSquadPlayerUseCase, UpdateSquadPlayerUseCase, RegisterMySquadProfileUseCase, RemoveSquadPlayerUseCase {

    private final UsersRepository usersRepository;
    private final ApplicationRepository applicationRepository;
    private final SquadRepository squadRepository;

    public SquadService(final UsersRepository usersRepository,
                        final ApplicationRepository applicationRepository,
                        final SquadRepository squadRepository) {
        this.usersRepository = usersRepository;
        this.applicationRepository = applicationRepository;
        this.squadRepository = squadRepository;
    }

    @Override
    public Squad create(final CreateSquadCommand command) {
        final Squad existedSquad = squadRepository.findByClubId(command.clubId());
        if (nonNull(existedSquad)) {
            return existedSquad;
        }
        final Squad squad = new Squad(getUlid().toString(), command.clubId(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now(), null);
        return squadRepository.save(squad);
    }

    @Override
    public Squad addPlayer(final AddFootBallPlayerCommand command) {
        final Squad squad = squad(command.clubId());
        if (squad.hasPlayer(command.userId())) {
            return squad;
        }
        final BackNumber backNumber = new BackNumber(randomNumber());
        final Application application = applicationRepository.findByClubAndUser(command.clubId(), command.userId());
        final SquadPlayer lineUpMember = squadPlayer(squad.getId(), command.userId(), application, backNumber);
        squad.addPlayer(lineUpMember);
        return squadRepository.save(squad);
    }

    @Override
    public Squad updatePlayer(final UpdateSquadPlayerCommand command) {
        final Squad squad = squad(command.clubId());
        if (!squad.hasPlayer(command.userId())) {
            throw new IllegalArgumentException();
        }
        final Users user = usersRepository.findById(command.userId());
        squad.updatePlayer(command.userId(), user.birthDate(),
                command.height(), command.weight(), command.foot(), command.positions(), command.backNumber()
        );
        return squadRepository.save(squad);
    }

    @Override
    public Squad registerMyProfile(final RegisterMySquadProfileCommand command) {
        final Squad squad = squad(command.clubId());
        if (!squad.hasPlayer(command.userId())) {
            throw new IllegalArgumentException();
        }
        squad.updatePlayer(command.userId(), null, command.height(), command.weight(), command.foot(), command.positions(), command.backNumber());
        return squadRepository.save(squad);
    }

    private Squad squad(final String clubId) {
        final Squad squad = squadRepository.findByClubId(clubId);
        if (nonNull(squad)) {
            return squad;
        }
        throw new IllegalArgumentException();
    }

    private SquadPlayer squadPlayer(final String squadId, String memberId, final Application application, final BackNumber backNumber) {
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

    @Override
    public void removePlayer(final RemoveSquadPlayerCommand command) {
        final Squad squad = squadRepository.findByClubId(command.clubId());
        if (squad == null || !squad.hasPlayer(command.userId())) {
            return;
        }
        squad.removePlayer(command.userId());
        squadRepository.save(squad);
    }

    private static int randomNumber() {
        return new Random().nextInt(99);
    }

}
