package com.official.lockr.domain.team.team.application;

import com.github.f4b6a3.ulid.UlidCreator;
import com.official.lockr.domain.team.team.application.command.RegisterTeamCommand;
import com.official.lockr.domain.team.team.domain.Team;
import com.official.lockr.domain.team.team.domain.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.official.lockr.domain.team.team.domain.Member.president;

@Service
public class TeamService implements RegisterTeamUseCase {

    private final TeamRepository teamRepository;

    public TeamService(final TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public Team register(final RegisterTeamCommand command) {
        final Team existedTeam = teamRepository.findByName(command.name());
        if (Objects.nonNull(existedTeam)) {
            throw new IllegalStateException();
        }
        final Team team = new Team(UlidCreator.getUlid().toString(), command.name(), command.description());
        team.addMember(president(UlidCreator.getUlid().toString(), command.userId()));
        return teamRepository.save(team);
    }
}
