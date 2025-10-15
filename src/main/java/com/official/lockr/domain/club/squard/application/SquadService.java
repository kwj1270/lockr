package com.official.lockr.domain.club.squard.application;

import com.official.lockr.domain.club.squard.application.dto.AddPlayerCommand;
import com.official.lockr.domain.club.squard.domain.*;
import com.official.lockr.domain.club.squard.domain.squad.Player;
import com.official.lockr.domain.club.squard.domain.squad.Squad;
import com.official.lockr.domain.club.squard.domain.squad.SquadRepository;
import com.official.lockr.domain.club.squard.domain.squad.vo.BackNumber;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

import static com.github.f4b6a3.ulid.UlidCreator.getUlid;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class SquadService implements AddPlayerUseCase {

    private final RecruitmentInfos recruitmentInfos;
    private final SquadRepository squadRepository;

    public SquadService(final RecruitmentInfos recruitmentInfos,
                        final SquadRepository squadRepository) {
        this.recruitmentInfos = recruitmentInfos;
        this.squadRepository = squadRepository;
    }

    @Override
    public Squad addPlayer(final AddPlayerCommand command) {
        final Squad squad = squad(command.teamId());
        if (squad.hasPlayer(command.memberId())) {
            return squad;
        }
        final BackNumber backNumber = new BackNumber(radomNumber());
        final RecruitmentInfo recruitmentInfo = recruitmentInfos.find(command.teamId(), command.userId());
        final Player player = player(squad.getId(), command.memberId(), recruitmentInfo, backNumber);
        squad.addPlayer(player);
        return squadRepository.save(squad);
    }

    private Squad squad(final String teamId) {
        final Squad squad = squadRepository.findByTeamId(teamId);
        if (nonNull(squad)) {
            return squad;
        }
        return new Squad(getUlid().toString(), teamId, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    private Player player(final String squadId, String memberId, final RecruitmentInfo recruitmentInfo, final BackNumber backNumber) {
        if (isNull(recruitmentInfo)) {
            return Player.init(getUlid().toString(), squadId, memberId, backNumber);
        }
        return Player.init(getUlid().toString(), squadId, memberId, recruitmentInfo, backNumber);
    }

    private static int radomNumber() {
        return new Random().nextInt(99);
    }
}
